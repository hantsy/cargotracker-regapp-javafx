package org.eclipse.cargotrakcer.regapp.cdi;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Qualifier
public @interface StageReadyEvent {

   Annotation Literal = new AnnotationLiteral<StageReadyEvent>() {};
}
