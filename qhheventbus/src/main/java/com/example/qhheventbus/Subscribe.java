package com.example.qhheventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinhaihang_vendor
 * @version $Rev$
 * @time 2019/3/5 15:40
 * @des
 * @packgename com.example.qhheventbus
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.POSTING;
}
