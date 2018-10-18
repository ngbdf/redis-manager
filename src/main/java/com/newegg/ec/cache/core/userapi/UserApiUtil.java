package com.newegg.ec.cache.core.userapi;

import com.google.common.collect.Lists;
import com.newegg.ec.cache.app.util.ClassUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lzz on 2018/4/22.
 */
public class UserApiUtil {
    public static final String GET_METHOD_NAME = "ajax.async_get";
    public static final String POST_METHOD_NAME = "ajax.async_post";

    public static void autoGeneriesAllApi(List<String> packages, String file) {
        Set<Class<?>> classSets = new HashSet<>();
        for (String p : packages) {
            Set<Class<?>> classSet = ClassUtil.getClasses(p);
            classSets.addAll(classSet);
        }
        String apiStr = "";
        for (Class<?> claz : classSets) {
            String controllerStr = createControllerApi(claz);
            if (!StringUtils.isBlank(controllerStr)) {
                apiStr += "/******************************** " + claz.getName() + " ********************************/\n";
                apiStr += controllerStr;
            }
        }

        writeStringToFile(file, apiStr);
    }

    private static String createControllerApi(Class<?> claz) {
        String apiStr = "";
        UserAccess userAccess = claz.getAnnotation(UserAccess.class);
        if (userAccess == null || !userAccess.autoCreate()) {
            return apiStr;
        }
        String controllerName = userAccess.name();
        Annotation[] annotations = claz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Controller) {
                RequestMapping requestMapping = claz.getAnnotation(RequestMapping.class);
                //获取基础路径
                String basePath = "";
                if (requestMapping != null) {
                    if (requestMapping.value() != null) {
                        basePath = requestMapping.value()[0];
                    }
                }
                Method[] methods = claz.getMethods();
                for (Method method : methods) {
                    apiStr += createMethodApi(basePath, method, controllerName);
                }
            }
        }
        return apiStr;
    }

    private static String createMethodApi(String basePath, Method method, String controllerName) {
        String methodName = "";
        String mapperPath = "";
        String requestType = "GET";
        List<String> paramsType = new ArrayList<>();
        List<String> params = new ArrayList<>();
        RequestMapping methodPath = method.getAnnotation(RequestMapping.class);
        ResponseBody methodReponse = method.getAnnotation(ResponseBody.class);
        Parameter[] parameters = method.getParameters();
        int removeId = -1;
        if (null != parameters) {
            for (int i = 0; i < parameters.length; i++) {
                SessionAttribute sessionAttribute = parameters[i].getDeclaredAnnotation(SessionAttribute.class);
                if (sessionAttribute != null) {
                    removeId = i;
                    break;
                }
            }
        }

        if (methodPath == null || methodReponse == null) {  //  有请求路径，和响应体说明是 ajax 请求
            return "";
        }
        //获取方法名
        methodName = method.getName();
        if (!StringUtils.isBlank(controllerName)) {
            methodName = controllerName + upperCaseFirst(methodName);
        }
        //获取方法路径
        mapperPath = methodPath.value()[0];
        //获取参数类型
        Class<?>[] types = method.getParameterTypes();
        String paramtype = "";
        for (Class c : types) {
            try {

                paramtype = c.getName();
                Object obj = c.newInstance();
                if (obj instanceof String) {
                    paramsType.add("String");
                } else {
                    paramsType.add(obj.toString());
                }
            } catch (Exception ignore) {
                paramsType.add(paramtype);
            }
        }
        //获取参数名
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramsArr = u.getParameterNames(method);
        if (null != paramsArr) {
            params = Lists.newArrayList(paramsArr);
            if (removeId != -1) {
                params.remove(removeId);
            }
        }
        //获取请求方式
        Annotation[][] annotationss = method.getParameterAnnotations();
        for (Annotation[] annotations : annotationss) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestBody) {
                    requestType = "POST";
                }
            }
        }
        return formatMethodStr(basePath, mapperPath, methodName, requestType, paramsType, params);
    }

    private static String formatMethodStr(String basePath, String mapperPath, String methodName, String requestType, List<String> paramsType, List<String> params) {
        String apiStr = "";
        String annotation = "/**";
        String methodStr = "function ";
        annotation += "\n * @type " + requestType;
        for (String paramtype : paramsType) {
            annotation += " \n * @param  " + paramtype;
        }
        annotation += "\n */";
        String funParam = StringUtils.join(params, ",");
        if (StringUtils.isBlank(funParam)) {
            methodStr += " " + methodName + "(callback){";
        } else {
            methodStr += " " + methodName + "(" + funParam + ",callback){";
        }

        if (requestType.equals("GET")) {
            String paramStr = "";
            for (int i = 0; i < params.size(); i++) {
                if (i == 0) {
                    paramStr += "?";
                }
                String endStr = "&";
                if (i == params.size() - 1) {
                    endStr = "";
                }
                paramStr += params.get(i) + "=\"+" + params.get(i) + "+\"" + endStr;
            }
            methodStr += "\n   " + GET_METHOD_NAME + "(\"" + basePath + mapperPath + paramStr + "\",callback);";
        } else {
            methodStr += "\n   " + POST_METHOD_NAME + "(\"" + basePath + mapperPath + "\"," + funParam + ",callback);";
        }
        methodStr += "\n}";
        apiStr = annotation + "\n" + methodStr + "\n";
        return apiStr;
    }

    public static void writeStringToFile(String filePath, String s) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(s.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String upperCaseFirst(String str) {
        char[] strChar = str.toCharArray();
        if ('a' < strChar[0] && strChar[0] < 'z') {
            strChar[0] = (char) (strChar[0] - 32);
        }
        return String.valueOf(strChar);
    }

}
