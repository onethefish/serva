package cn.fish.cloud.serva.web.context;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"common-java:DuplicatedBlocks"})
@Component
public class SpringContext implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext appContext) {
        setContext(appContext);
    }

    private static void setContext(ApplicationContext appContext) {
        applicationContext = appContext;
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static String[] getBeanNamesForType(Class<?> type) {
        return applicationContext.getBeanNamesForType(type);
    }

    public static <T> Set<T> getBeans(Class<T> clazz) {
        Map<String, T> beanMap =
                applicationContext.getBeansOfType(clazz);
        if (beanMap.isEmpty()) {
            return new HashSet<>();
        }
        Set<T> sets = new HashSet<>();
        beanMap.forEach((k, v) -> sets.add(v));
        return sets;
    }

    public static <T> Map<String, T> getBeansMap(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

}
