package com.bank.account.config;

import com.fasterxml.jackson.databind.JavaType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

/**
 * Unwraps RxJava 3 types so SpringDoc documents {@code T} instead of empty wrappers.
 */
public class RxJava3ModelConverter implements ModelConverter {

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        JavaType javaType = Json.mapper().constructType(type.getType());
        if (javaType != null) {
            Class<?> raw = javaType.getRawClass();
            if (isMultiValue(raw) && javaType.containedTypeCount() > 0) {
                Schema<?> item = context.resolve(copy(type, javaType.containedType(0)));
                return new ArraySchema().items(item);
            }
            if (isSingleValue(raw) && javaType.containedTypeCount() > 0) {
                return context.resolve(copy(type, javaType.containedType(0)));
            }
            if (Completable.class.isAssignableFrom(raw)) {
                return null;
            }
        }
        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }

    private static AnnotatedType copy(AnnotatedType original, JavaType inner) {
        return new AnnotatedType(inner)
                .schemaProperty(original.isSchemaProperty())
                .resolveAsRef(original.isResolveAsRef());
    }

    private static boolean isMultiValue(Class<?> raw) {
        return Observable.class.isAssignableFrom(raw) || Flowable.class.isAssignableFrom(raw);
    }

    private static boolean isSingleValue(Class<?> raw) {
        return Single.class.isAssignableFrom(raw) || Maybe.class.isAssignableFrom(raw);
    }
}
