package org.springframework.shantaomvc.type;


import java.util.Objects;

/**
 * 存储http请求路径和请求方法
 * 注意：要重写hashcode和equals方法
 *
 * @author MashanTao
 * @date 2021/11/26
 */
public class RequestPathInfo {
    // http请求方法
    private String httpMethod;

    // http请求路径
    private String httpPath;

    public RequestPathInfo(String httpMethod, String httpPath) {
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public void setHttpPath(String httpPath) {
        this.httpPath = httpPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestPathInfo that = (RequestPathInfo) o;
        return Objects.equals(httpMethod, that.httpMethod) && Objects.equals(httpPath, that.httpPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, httpPath);
    }
}
