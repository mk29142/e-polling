import spark.ResponseTransformer;
import com.google.gson.Gson;

class JsonTransformer implements ResponseTransformer {
    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

    public <T> T fromJson(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }
}