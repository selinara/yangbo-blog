package com.chl.gbo.cental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.thymeleaf.util.StringUtils;

import com.chl.gbo.cental.component.AccessDecisionManagerImpl;
import com.chl.gbo.cental.component.FilterInvocationSecurityMetadataSourceImpl;
import com.chl.gbo.cental.handle.SpringSecurityAccessDeniedHandle;
import com.chl.gbo.cental.handle.SpringSecurityFailureHandle;
import com.chl.gbo.cental.handle.SpringSecuritySuccessHandle;
import com.chl.gbo.cental.service.UserService;

/**
 * @Auther: BoYanG
 * @Describe 配置类
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    //根据一个url请求，获得访问它所需要的roles权限
    @Autowired
    private FilterInvocationSecurityMetadataSourceImpl filterInvocationSecurityMetadataSource;

    //接收一个用户的信息和访问一个url所需要的权限，判断该用户是否可以访问
    @Autowired
    private AccessDecisionManagerImpl accessDecisionManager;


    //403页面
    @Autowired
    private SpringSecurityAccessDeniedHandle ssAccessDeniedHandler;
    //登录失败的处理
    @Autowired
    private SpringSecurityFailureHandle springSecurityFailureHandle;
    //登录成功的处理
    @Autowired
    private SpringSecuritySuccessHandle springSecuritySuccessHandle;


    /**定义认证用户信息获取来源，密码校验规则等*/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**有以下几种形式，使用第3种*/
        //inMemoryAuthentication 从内存中获取
        //auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder()).withUser("user1").password(new BCryptPasswordEncoder().encode("123123")).roles("USER");

        //jdbcAuthentication从数据库中获取，但是默认是以security提供的表结构
        //usersByUsernameQuery 指定查询用户SQL
        //authoritiesByUsernameQuery 指定查询权限SQL
        //auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(query).authoritiesByUsernameQuery(query);

        //注入userDetailsService，需要实现userDetailsService接口
        //auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder(4));

        auth.authenticationProvider(this.daoAuthenticationProvider());
    }

    //在这里配置哪些页面不需要认证，添加在此处的URL直接走控制器，不走授权认证！！！
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/", "/static/**");
    }



    /**定义安全策略*/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()       //配置安全策略
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setSecurityMetadataSource(filterInvocationSecurityMetadataSource);
                        o.setAccessDecisionManager(accessDecisionManager);
                        return o;
                    }
                })
//                .antMatchers("/hello").hasAuthority("ADMIN")
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/index").failureUrl("/login?error")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
//                .failureHandler(springSecurityFailureHandle)
//                .successHandler(springSecuritySuccessHandle)
                .and()
                .headers().frameOptions().disable()
                .and()
                .logout().logoutUrl("/logout")
                .permitAll()
                .and()
                .csrf()
                .disable()
                .exceptionHandling().accessDeniedPage("/no/permission");
//                .accessDeniedHandler(myAccessDeniedHandler);
    }

    /**
     * 构建自定义的DaoAuthenticationProvider
     * @return
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider bean = new DaoAuthenticationProvider();
        bean.setHideUserNotFoundExceptions(false);
        bean.setUserDetailsService(this.userService);
        bean.setPasswordEncoder(new BCryptPasswordEncoder(4) {

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {

                // 为空逻辑前端判断，后台只校验账密是否有误
                try {
                    if (StringUtils.isEmpty(rawPassword.toString())
                            || StringUtils.isEmpty(encodedPassword)
                            || !BCrypt.checkpw(rawPassword.toString(), encodedPassword)) {
                        throw new BadCredentialsException("用户名或密码错误");
                    }
                    return true;
                } catch (IllegalArgumentException e) {
                    throw new BadCredentialsException("用户名或密码错误");
                }
            }

        });
        return bean;
    }

}
