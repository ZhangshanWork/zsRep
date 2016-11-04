package im.vinci.server.utils.json;

import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;

public class DisablingMaskFieldIntrospector extends JacksonAnnotationIntrospector {
 
  @Override
  public boolean isAnnotationBundle(Annotation ann) {
    if (ann.annotationType().equals(MaskField.class)) {
      return false;
    } else {
      return super.isAnnotationBundle(ann);
    }
  }
 
}