package pl.edu.agh.pmakarewicz.moviestars;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
public class Controller {

    @Autowired
    private FreemarkerConfiguration configuration;

    @GetMapping("/")
    public RedirectView getIndex(){
        return new RedirectView("index.html");
    }

    @GetMapping("/results")
    public String getResults(@RequestParam String firstname, @RequestParam String lastname){
        Map<String, Object> model = new HashMap<>();
        model.put("firstname", firstname);
        model.put("lastname", lastname);

        String bio = getBio(firstname, lastname); // TODO: asynchronicznie
        // TODO: zdjęcie z Google Photos
        String imageLink = getImageLink(firstname, lastname);
        // TODO: jakieś summary z API o Filmach

        model.put("bio", bio);
        model.put("imagelink", imageLink);
        try {
            Template template = configuration.getTemplate("results.html");
            Writer stringWriter = new StringWriter();
            template.process(model, stringWriter);
            String responseStr = stringWriter.toString();
            stringWriter.close();
            return responseStr;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong...");
        }

    }

    private String getBio(String firstname, String lastname) {
        try {
            String jsonStr = performRequestToWikipedia(firstname, lastname);
            Gson gson = new Gson();
            JsonElement root = gson.fromJson(jsonStr, JsonElement.class);
            JsonObject entries = root.getAsJsonObject().get("query").getAsJsonObject().get("pages").getAsJsonObject();
            String key = entries.keySet().iterator().next();
            JsonObject entry = entries.getAsJsonObject(key);
            return entry.get("extract").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unfortunately, we could not find a Wikipedia bio for this movie star.";
        }
    }

    private String getImageLink(String firstname, String lastname){
        try {
        String jsonStr = performRequestToSerpapi(firstname, lastname);
        Gson gson = new Gson();
        JsonElement root = gson.fromJson(jsonStr, JsonElement.class);
        return root.getAsJsonObject()
                .get("images_results").getAsJsonObject()
                .get("0").getAsJsonObject()
                .get("original").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return "https://via.placeholder.com/100";
        }
    }

    private String performRequestToWikipedia(String firstname, String lastname) throws IOException {
        URL url = new URL(String.format("https://en.wikipedia.org/w/api.php?titles=%s+%s&action=query&prop=extracts&explaintext=true&format=json&exsectionformat=plain&exintro=true", firstname, lastname));
        return performRequest(url);

    }
    private String performRequestToSerpapi(String firstname, String lastname) throws IOException {
        String serpapiKey = System.getProperties().getProperty("serpapi_key");
        URL url = new URL(String.format("https://serpapi.com/search.json?q=%s+%s&tbm=isch&ijn=0&api_key=%s", firstname, lastname, serpapiKey));
        return performRequest(url);
    }

    private String performRequest(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }
}
