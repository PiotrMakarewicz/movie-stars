package pl.edu.agh.pmakarewicz.moviestars;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FreemarkerConfiguration extends Configuration {
    FreemarkerConfiguration() throws IOException {
        super(Configuration.VERSION_2_3_23);

// Specify the source where the template files come from. Here I set a
// plain directory for it, but non-file-system sources are possible too:
        this.setDirectoryForTemplateLoading(new File("src/main/resources/static/"));

// From here we will set the settings recommended for new projects. These
// aren't the defaults for backward compatibilty.

// Set the preferred charset template files are stored in. UTF-8 is
// a good choice in most applications:
        this.setDefaultEncoding("UTF-8");

// Sets how errors will appear.
// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        this.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        this.setLogTemplateExceptions(false);

// Wrap unchecked exceptions thrown during template processing into TemplateException-s:
//        super.setWrapUncheckedExceptions(true);

// Do not fall back to higher scopes when reading a null loop variable:
//        this.setFallbackOnNullLoopVariable(false);
    }
}
