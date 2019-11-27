package gov.nasa.jpl.ammos.mpsa.aerie.merlincli.commands.impl.adaptation;

import gov.nasa.jpl.ammos.mpsa.aerie.merlincli.commands.Command;
import gov.nasa.jpl.ammos.mpsa.aerie.merlincli.models.HttpHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlincli.utils.JSONUtilities.prettify;

public class GetActivityTypeParameterListCommand implements Command {

    private HttpHandler httpClient;
    private String adaptationId;
    private String activityTypeId;
    private String responseBody;
    private int status;

    public GetActivityTypeParameterListCommand(HttpHandler httpClient, String adaptationId, String activityTypeId) {
        this.httpClient = httpClient;
        this.adaptationId = adaptationId;
        this.activityTypeId = activityTypeId;
        this.status = -1;
    }

    public void execute() {
        String url = String.format("http://localhost:27182/api/adaptations/%s/activities/%s/parameters", this.adaptationId, this.activityTypeId);
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = this.httpClient.execute(request);

            this.status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                this.responseBody = prettify(response.getEntity().toString());
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public int getStatus() {
        return status;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
