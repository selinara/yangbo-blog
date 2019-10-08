package com.chl.gbo.cental.handle;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * @Auther: BoYanG
 * @Describe SpringSecurity 登录失败的处理
 */
@Component
public class SpringSecurityFailureHandle implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
            httpServletResponse.setContentType("application/json;charset=utf-8");
            PrintWriter out = httpServletResponse.getWriter();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"status\":\"error\",\"msg\":\"");
            if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                sb.append("用户名或密码输入错误，登录失败!");
            } else if (e instanceof DisabledException) {
                sb.append("账户被禁用，登录失败，请联系管理员!");
            } else {
                sb.append("登录失败!");
            }
            sb.append("\"}");
            out.write(sb.toString());
            out.flush();
            out.close();
    }
}
