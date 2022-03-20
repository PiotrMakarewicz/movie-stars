package pl.edu.agh.pmakarewicz.moviestars;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
public class Controller {

    @Autowired
    private FreemarkerConfiguration configuration;

    @Value("${serpapi_key}")
    private String serpapiKey;

    @Value("${celebapi_key}")
    private String celebapiKey;

    @GetMapping("/")
    public RedirectView getIndex(){
        return new RedirectView("index.html");
    }

    @GetMapping("/results")
    public String getResults(@RequestParam String firstname, @RequestParam String lastname){
        Map<String, Object> model = new HashMap<>();
        model.put("firstname", firstname);
        model.put("lastname", lastname);

        //TODO: asynchronicznie
        String bio = getBio(firstname, lastname);
        String imageLink = getImageLink(firstname, lastname);

        Map<String, String> details = getPersonDetails(firstname, lastname);

        model.put("bio", bio);
        model.put("imagelink", imageLink);
        model.putAll(details);
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
                .get("images_results").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("original").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return "https://via.placeholder.com/100";
        }
    }

    private Map<String, String> getPersonDetails(String firstname, String lastname) {
        Map<String, String> map = new HashMap<>();
        try {
            String jsonStr = performRequestToCeleb(firstname, lastname);
//            System.out.println(jsonStr);
            Gson gson = new Gson();
            JsonArray rootArr = gson.fromJson(jsonStr, JsonArray.class);
            JsonObject obj = rootArr.get(0).getAsJsonObject();

            map.put("gender", obj.get("gender").getAsString());
            map.put("age", obj.get("age").getAsString());
            map.put("height", obj.get("height").getAsString());
            map.put("nationality", obj.get("nationality").getAsString());
            map.put("net_worth", obj.get("net_worth").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            map.put("gender", "unknown");
            map.put("age", "unknown");
            map.put("height", "unknown");
            map.put("nationality", "unknown");
            map.put("net_worth", "unknown");

        }
        return map;
    }

        private String performRequestToWikipedia (String firstname, String lastname) throws IOException {
            URL url = new URL(String.format("https://en.wikipedia.org/w/api.php?titles=%s+%s&action=query&prop=extracts&explaintext=true&format=json&exsectionformat=plain&exintro=true", firstname, lastname));
            return performRequest(url);

        }
        private String performRequestToSerpapi (String firstname, String lastname) throws IOException {
            URL url = new URL(String.format("https://serpapi.com/search.json?q=%s+%s&tbm=isch&ijn=0&api_key=%s", firstname, lastname, serpapiKey));
            return performRequest(url);
        }

        private String performRequestToCeleb (String firstname, String lastname) throws IOException {
            URL url = new URL(String.format("https://api.celebrityninjas.com/v1/search?name=%s+%s", firstname, lastname));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Api-Key", celebapiKey);
            return fetchResponse(con);
        }

        private String fetchResponse (HttpURLConnection con) throws IOException {
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

        private String performRequest (URL url) throws IOException {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            return fetchResponse(con);
        }
    }
