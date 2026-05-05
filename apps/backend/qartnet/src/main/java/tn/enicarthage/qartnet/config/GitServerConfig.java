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
        
        ServletRegistrationBean<GitServlet> bean = new ServletRegistrationBean<>(servlet, "/git/*");
        // Activer le push (receivepack) et le pull (uploadpack)
        bean.addInitParameter("http.getreceivepack", "true");
        bean.addInitParameter("http.getuploadpack", "true");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
