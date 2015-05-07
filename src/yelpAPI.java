import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class yelpAPI {


	private static class YelpAPICLI {
		@Parameter(names = {"-q", "--term"}, description = "Search Query Term")
		public String term = DEFAULT_TERM;

		@Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
		public String location = DEFAULT_LOCATION;
		
		@Parameter(names = {"-lati"}, description = "latitude")
		public String latitude = DEFAULT_latitude;
		
		@Parameter(names = {"-long"}, description = "longtitude")
		public String longtitude = DEFAULT_longtitude;
		
		@Parameter(names = {"-r"}, description = "longtitude")
		public String radius = DEFAULT_radius;
		
		
		public String coordinate = latitude+","+longtitude;
	}

//40.8075, 73.9619
	public static void main(String[] args) {
	    YelpAPICLI yelpApiCli = new YelpAPICLI();
	    String[] arv = {"-q" , "sushi", "-l","New York, NY", "-lati","40.8075", "-long", "73.9619","-r" , "1000" } ;
	    new JCommander(yelpApiCli, arv);
	    
	    yelpAPI yelpApi = new yelpAPI();
	    queryAPI(yelpApi, yelpApiCli);
	  }

	
	

	  /**
	   * Creates and sends a request to the Search API by term and location.
	   * <p>
	   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
	   * for more info.
	   * 
	   * @param term <tt>String</tt> of the search term to be queried
	   * @param location <tt>String</tt> of the location
	   * @return <tt>String</tt> JSON Response
	   */
	  public String searchForBusinessesByLocation(String term, String coordinate, String radius,String location) {
	    OAuthRequest request = createOAuthRequest(SEARCH_PATH);
	    request.addQuerystringParameter("term", term);
	    request.addQuerystringParameter("location",location);
	    request.addQuerystringParameter("cll", coordinate);
	    request.addQuerystringParameter("radius_filter", radius);
	    request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
	    return sendRequestAndGetResponse(request);
	  }

	
	
	
	/**
	   * Creates and sends a request to the Business API by business ID.
	   * <p>
	   * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
	   * for more info.
	   * 
	   * @param businessID <tt>String</tt> business ID of the requested business
	   * @return <tt>String</tt> JSON Response
	   */
	  public String searchByBusinessId(String businessID) {
	    OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
	    return sendRequestAndGetResponse(request);
	  }
	
	
	/**
	   * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
	   * 
	   * @param path API endpoint to be queried
	   * @return <tt>OAuthRequest</tt>
	   */
	  private OAuthRequest createOAuthRequest(String path) {
	    OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
	    return request;
	  }
	
	
	/**
	   * Sends an {@link OAuthRequest} and returns the {@link Response} body.
	   * 
	   * @param request {@link OAuthRequest} corresponding to the API request
	   * @return <tt>String</tt> body of API response
	   */
	  private String sendRequestAndGetResponse(OAuthRequest request) {
	    System.out.println("Querying " + request.getCompleteUrl() + " ...");
	    this.service.signRequest(this.accessToken, request);
	    Response response = request.send();
	    return response.getBody();
	  }
	
	
	/**
	   * Queries the Search API based on the command line arguments and takes the first result to query
	   * the Business API.
	   * 
	   * @param yelpApi <tt>YelpAPI</tt> service instance
	   * @param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
	   */
	  private static void queryAPI(yelpAPI yelpApi, YelpAPICLI yelpApiCli) {
		 
	    String searchResponseJSON =
	        yelpApi.searchForBusinessesByLocation(yelpApiCli.term,yelpApiCli.coordinate,yelpApiCli.radius, yelpApiCli.location);

	    JSONParser parser = new JSONParser();
	    JSONObject response = null;
	    try {
	      response = (JSONObject) parser.parse(searchResponseJSON);
	    } catch (ParseException pe) {
	      System.out.println("Error: could not parse JSON response:");
	      System.out.println(searchResponseJSON);
	      System.exit(1);
	    }
	    
	    JSONArray businesses = (JSONArray) response.get("businesses");
	    
	    JSONObject firstBusiness = (JSONObject) businesses.get(0);
	    
	    String firstBusinessID = firstBusiness.get("id").toString();
	    
	    
	    System.out.println(String.format(
	        "%s businesses found, querying business info for the top result \"%s\" ...",
	        businesses.size(), firstBusinessID));

	    // Select the first business and display business details
	    String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
	    JSONObject businessOBject = null;
	    try {
			businessOBject = (JSONObject) parser.parse(businessResponseJSON);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    
	    System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
	    System.out.println(businessResponseJSON);
	  }
	
	
	private static final String API_HOST = "api.yelp.com";
	private static final String DEFAULT_TERM = "dinner";
	private static final String DEFAULT_LOCATION = "New York, NY";
	private static final String DEFAULT_latitude = "40.8075";
	private static final String DEFAULT_longtitude ="73.9619";
	private static final String DEFAULT_radius = "500";
	private static final int SEARCH_LIMIT = 3;
	private static final String SEARCH_PATH = "/v2/search";
	private static final String BUSINESS_PATH = "/v2/business";

	private String  consumerKey = "A80mMWJKf-Y9W1rpUhI_FA";
	private String  consumerSecret = "VF4omAE2IqxemjnCblGy5uKXPcM";
	private String  token = "555fCMynTkvyIva5uE2V7L5CJhEnN4nT";
	private String  tokenSecret = "beILitqROursMjGK_qUVacAczw4";



	
	public OAuthService service =  new ServiceBuilder().provider(twoWayAuthentication.class).apiKey(consumerKey)
			.apiSecret(consumerSecret).build();;
			public Token accessToken = new Token(token, tokenSecret);



}
