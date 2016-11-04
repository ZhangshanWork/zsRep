package im.vinci.server.common.exceptions.handler;

import im.vinci.server.common.exceptions.*;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.common.exceptions.error.ErrorInfo;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Objects;

/**
 * Created by henryhome on 3/31/15.
 */
@ControllerAdvice(basePackages = "im.vinci.server")
public class BasicExceptionHandler {
    public Logger logger = LoggerFactory.getLogger(getClass());

    //global
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleExceptions(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorInfo(ErrorCode.INTERNAL_SERVER_ERROR, ex.toString(), "内部未知错误");
    }


    @ExceptionHandler(value = VinciAuthenticationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorInfo handleAuthenticationExceptions(VinciAuthenticationException ex) {
        logger.error(ex.toString());
        return new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg(), ex.getErrorMsgToUser());
    }

    @ExceptionHandler(value = VinciException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleDeviceExceptions(VinciException ex) {
        if (Objects.equals(ex.getErrorCode(), ErrorCode.INTERNAL_SERVER_ERROR)) {
            logger.error(ex.toString(), ex);
        } else {
            logger.error(ex.toString());
        }
        return new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg(), ex.getErrorMsgToUser());
    }

    @ExceptionHandler(value = {InvalidParameterException.class, ParameterMissingException.class,
        ServerErrorException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleGeneralVinciExceptions(VinciException ex) {
        if (Objects.equals(ex.getErrorCode(), ErrorCode.INTERNAL_SERVER_ERROR)) {
            logger.error(ex.toString(), ex);
        } else {
            logger.error(ex.toString());
        }
        return new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg(), ex.getErrorMsgToUser());
    }

    @ExceptionHandler(value = {java.security.InvalidParameterException.class, IllegalArgumentException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleGeneralVinciExceptions(IllegalArgumentException ex) {
        logger.warn(ex.getMessage());
        return new ErrorInfo(ErrorCode.ARGUMENT_ERROR, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,ServletRequestBindingException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorInfo(ErrorCode.BAD_REQUEST, ex.getMessage(), "参数不全");
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleTypeMismatch(TypeMismatchException ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorInfo(ErrorCode.BAD_REQUEST, ex.getMessage(), "参数类型不匹配");
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorInfo(ErrorCode.BAD_REQUEST, ex.getMessage(), "JSON格式不对");
    }

    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo handleHttpMessageNotReadable(HttpMediaTypeNotSupportedException ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorInfo(ErrorCode.BAD_REQUEST, ex.getMessage(), ex.getMessage());
    }


    @ExceptionHandler(value = {NullPointerException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleInternalServer(Exception ex) {
        logger.error(ex.getMessage(), ex);
        ServerErrorException exception = new ServerErrorException();
        return new ErrorInfo(exception.getErrorCode(), exception.getErrorMsg(), exception.getErrorMsgToUser());
    }

    @ExceptionHandler(value = {BadSqlGrammarException.class, MyBatisSystemException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorInfo handleBadSqlGrammar(BadSqlGrammarException ex) {
        logger.error(ex.getMessage(), ex);
        DatabaseException exception = new DatabaseException();
        return new ErrorInfo(exception.getErrorCode(), exception.getErrorMsg(), exception.getErrorMsgToUser());
    }
}



