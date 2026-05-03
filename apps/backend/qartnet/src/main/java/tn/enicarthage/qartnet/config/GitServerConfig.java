package tn.enicarthage.qartnet.config;

import org.eclipse.jgit.http.server.GitServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitServerConfig {

    private final QartnetRepositoryResolver repositoryResolver;

    public GitServerConfig(QartnetRepositoryResolver repositoryResolver) {
        this.repositoryResolver = repositoryResolver;
    }

    @Bean
    public ServletRegistrationBean<GitServlet> gitServlet() {
        GitServlet servlet = new GitServlet();
        servlet.setRepositoryResolver(repositoryResolver);
        
        // La servlet écoutera sur toutes les URL commençant par /git/
        ServletRegistrationBean<GitServlet> bean = new ServletRegistrationBean<>(servlet, "/git/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
