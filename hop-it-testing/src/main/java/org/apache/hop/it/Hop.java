package org.apache.hop.it;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Hop {
  HopEngine value() default HopEngine.local;

  boolean lazy() default false;

  HopEngine[] excludes() default {HopEngine.Dataflow};
}
