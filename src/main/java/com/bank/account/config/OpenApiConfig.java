package com.bank.account.config;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.parsers.ReturnTypeParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenAPI documentation configuration for the account service.
 */
@Configuration
public class OpenApiConfig {

    private static final AtomicBoolean CONVERTER_REGISTERED = new AtomicBoolean();

    static {
        if (CONVERTER_REGISTERED.compareAndSet(false, true)) {
            ModelConverters.getInstance().addConverter(new RxJava3ModelConverter());
        }
    }

    /**
     * Builds the OpenAPI descriptor.
     *
     * @return OpenAPI bean
     */
    @Bean
    public OpenAPI accountOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .description("Passive products: savings, checking and fixed-term accounts")
                        .version("1.0.0"));
    }

    /**
     * Maps RxJava 3 return types to Mono/Flux so SpringDoc WebFlux can document them.
     *
     * @return return type parser
     */
    @Bean
    public ReturnTypeParser rxJava3ReturnTypeParser() {
        return new ReturnTypeParser() {
            @Override
            public Type getReturnType(MethodParameter methodParameter) {
                return unwrapRxJava(ReturnTypeParser.super.getReturnType(methodParameter));
            }
        };
    }

    private static Type unwrapRxJava(Type type) {
        if (type instanceof Class<?> raw && Completable.class.isAssignableFrom(raw)) {
            return Void.TYPE;
        }
        if (!(type instanceof ParameterizedType parameterized)) {
            return type;
        }
        if (!(parameterized.getRawType() instanceof Class<?> raw)) {
            return type;
        }
        Type inner = parameterized.getActualTypeArguments()[0];
        if (Observable.class.isAssignableFrom(raw) || Flowable.class.isAssignableFrom(raw)) {
            return new MappedType(Flux.class, inner);
        }
        if (Single.class.isAssignableFrom(raw) || Maybe.class.isAssignableFrom(raw)) {
            return new MappedType(Mono.class, inner);
        }
        return type;
    }

    private record MappedType(Class<?> raw, Type argument) implements ParameterizedType {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{argument};
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
