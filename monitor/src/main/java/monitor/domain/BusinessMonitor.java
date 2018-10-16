package monitor.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface BusinessMonitor {
	String topicName() default "";

	String needSendMqMessage() default "ret.getResult()==\"0\"";

	String messageBody() default "monitor.domain.BaseMessageBody";

	String[] initMessageBody() default { "" };
}
