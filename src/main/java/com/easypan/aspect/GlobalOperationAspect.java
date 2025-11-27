package com.easypan.aspect;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.enums.ResponseCodeEnum;
import com.easypan.exception.BusinessException;
import com.easypan.utils.StringTools;
import com.easypan.utils.VerifyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    // 支持校验的简单类型名称
    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_LONG = "java.lang.Long";
    private static final String TYPE_INTEGER = "java.lang.Integer";

    // 定义切点，拦截所有标注了 @GlobalInterceptor 注解的方法
    @Pointcut("@annotation(com.easypan.annotation.GlobalInterceptor)")
    private void requestInterceptor(){}

    /**
     * 前置通知，方法执行前触发
     * 通过反射获取方法上的 @GlobalInterceptor 注解，根据注解属性决定是否进行参数校验
     */
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint joinPoint) throws BusinessException {
        try{
            // 获取目标对象和方法参数
            Object target = joinPoint.getTarget();
            Object[] args = joinPoint.getArgs();
            String methodName = joinPoint.getSignature().getName();

            // 获取方法参数类型，反射获取具体方法对象
            Class<?>[] parameterType = ((MethodSignature)joinPoint.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterType);

            // 获取方法上的 @GlobalInterceptor 注解实例
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if(interceptor == null){
                // 没有注解则跳过，不做校验
                return;
            }

            /**
             * 校验登录
             */
            if(interceptor.checkLogin()||interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }

            /**
             * 判断注解中的 checkParams 是否为 true，
             * 如果是则执行参数校验逻辑
             */
            if(interceptor.checkParams()){
                validateParams(method,args);
            }
        }catch (BusinessException e){
            // 业务异常直接抛出
            logger.error("全局拦截器异常",e);
            throw e;
        } catch (Exception e) {
            // 其他异常统一封装成业务异常返回
            logger.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            // 捕获所有错误，防止漏掉异常
            logger.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * 逐个参数校验方法
     * @param method 反射得到的方法对象
     * @param args  方法参数实际传入的值数组
     */
    private void validateParams(Method method, Object[] args) throws BusinessException {
        Parameter[] parameters = method.getParameters();
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = args[i];

            // 获取参数上的 @VerifyParam 注解，用来定义校验规则
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if(verifyParam == null){
                // 没有该注解，则跳过该参数，不校验
                continue;
            }

            // 判断参数类型，简单类型直接校验，复杂对象递归校验字段
            if(TYPE_STRING.equals(parameter.getParameterizedType().getTypeName())||
                    TYPE_LONG.equals(parameter.getParameterizedType().getTypeName())||
                    TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())){
                checkValue(value,verifyParam);
            }else{
                checkObjValue(parameter,value);
            }
        }
    }

    /**
     * 复杂对象字段递归校验
     * 通过反射获取参数的所有字段，对带有 @VerifyParam 注解的字段逐一校验
     * @param parameter 方法参数元数据
     * @param value 参数实际对象实例
     */
    private void checkObjValue(Parameter parameter,Object value){
        try{
            // 获取参数的完整类型名，并加载类
            String typeName = parameter.getParameterizedType().getTypeName();
            Class classz = Class.forName(typeName);

            // 获取类所有声明字段
            Field[]  fields = classz.getDeclaredFields();
            for(Field field : fields){
                // 获取字段上的 @VerifyParam 注解
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if(fieldVerifyParam == null){
                    // 无注解，跳过字段
                    continue;
                }

                // 设置字段可访问，防止 private 字段访问异常
                field.setAccessible(true);

                // 获取字段值
                Object resultValue = field.get(value);

                // 对字段值执行校验
                checkValue(resultValue,fieldVerifyParam);
            }
        }catch (BusinessException e){
            logger.error("校验参数失败",e);
            throw e;
        } catch (Exception e) {
            logger.error("校验参数失败",e);
            // 反射异常封装成业务异常返回
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    /**
     * 基础校验逻辑：判断是否为空、长度是否合规、正则是否匹配
     * @param value 参数值
     * @param verifyParam 校验注解实例，包含规则配置
     */
    private void checkValue(Object value,VerifyParam verifyParam)throws BusinessException{
        Boolean isEmpty = value==null|| StringTools.isEmpty(value.toString());
        Integer length = value==null?0:value.toString().length();

        /**
         * 校验必填项
         */
        if(isEmpty&&verifyParam.required()){
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        /**
         * 校验长度范围
         */
        if(!isEmpty&&(verifyParam.max()!=-1&&verifyParam.max()<length||verifyParam.min()!=-1&&verifyParam.min()>length)){
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }

        /**
         * 校验正则表达式
         * 需要先判断正则配置不为空，再校验
         */
        if(!isEmpty&&!StringTools.isEmpty(verifyParam.regex().getRegex())&&!VerifyUtils.verify(verifyParam.regex(),String.valueOf(value))){
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    private void checkLogin(Boolean checkAdmin){
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if(sessionWebUserDto == null){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if(checkAdmin&&!sessionWebUserDto.getAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
