package org.ndexbio.communitydetection.rest.services; // Note your package will be {{ groupId }}.rest

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.engine.CommunityDetectionEngine;
import org.ndexbio.communitydetection.rest.model.CXMateResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;
import org.ndexbio.communitydetection.rest.model.Task;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionBadRequestException;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 * CommunityDetection service
 * @author churas
 */
@OpenAPIDefinition( info = 
    @Info(title = "Community Detection REST service",
          version = "0.8.1-SNAPSHOT",
          description = "This service lets caller invoke various community detection clustering "
                  + "algorithms which have been packaged into Docker images. To see what "
                  + "algorithms are supported visit the 'algorithms' endpoint below.\n\n "
                  + "<b>NOTE:</b> This service is experimental. The interface is subject to change.\n" +
"")
)
@Server(
        description = "default",
        url = "/cd" + Configuration.APPLICATION_PATH
        )
@Path("/")
public class CommunityDetection {
    
    static Logger logger = LoggerFactory.getLogger(CommunityDetection.class);
    
	@GET
	@Path(Configuration.LEGACY_DIFFUSION_PATH + "/")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden = true,
			   summary = "Legacy endpoint to let user know NOT to use this endpoint",
			   description="This endpoint exists to maintain consistency with legacy "
					   + "diffusion service that returns 405 error and json response",
			   responses = {
				   @ApiResponse(responseCode = "405", 
						        description = "The response will be JSON with no data "
										+ "and a single error denoting this endpoint "
										+ " should not be used.",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = CXMateResult.class)))
			   })
	public Response legacyDiffusionGet(){
		CXMateResult result = new CXMateResult("you must use the POST method with this endpoint",
		null, Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), null);
		 return Response.status(Response.Status.METHOD_NOT_ALLOWED).type(MediaType.APPLICATION_JSON)
					.entity(result.asJson()).build();
	}
	
	@DELETE
	@Path(Configuration.LEGACY_DIFFUSION_PATH + "/")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden = true,
			   summary = "Legacy endpoint to let user know NOT to use this endpoint",
			   description="This endpoint exists to maintain consistency with legacy "
					   + "diffusion service that returns 405 error and json response",
			   responses = {
				   @ApiResponse(responseCode = "405", 
						        description = "The response will be JSON with no data "
										+ "and a single error denoting this endpoint "
										+ " should not be used.",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = CXMateResult.class)))
			   })
	public Response legacyDiffusionDelete(){
		CXMateResult result = new CXMateResult("you must use the POST method with this endpoint",
		null, Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), null);
		 return Response.status(Response.Status.METHOD_NOT_ALLOWED).type(MediaType.APPLICATION_JSON)
					.entity(result.asJson()).build();
	}
	
	@PUT
	@Path(Configuration.LEGACY_DIFFUSION_PATH + "/")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden = true,
			   summary = "Legacy endpoint to let user know NOT to use this endpoint",
			   description="This endpoint exists to maintain consistency with legacy "
					   + "diffusion service that returns 405 error and json response",
			   responses = {
				   @ApiResponse(responseCode = "405", 
						        description = "The response will be JSON with no data "
										+ "and a single error denoting this endpoint "
										+ " should not be used.",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = CXMateResult.class)))
			   })
	public Response legacyDiffusionPut(){
		CXMateResult result = new CXMateResult("you must use the POST method with this endpoint",
		null, Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), null);
		 return Response.status(Response.Status.METHOD_NOT_ALLOWED).type(MediaType.APPLICATION_JSON)
					.entity(result.asJson()).build();
	}
	
	@POST
	@Path(Configuration.LEGACY_DIFFUSION_PATH + "/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Legacy endpoint to run network propagation",
			   description="Payload must be a CX network containing the nodes, "
					   + "edges and nodeAttributes aspects, as described in Request Body below.",
			   responses = {
				   @ApiResponse(responseCode = "200", 
						        description = "The response body will contain a CX network "
										+ "containing the nodes, edges, and nodeAttributes aspects. "
										+ "Each node will have two associated attributes, "
										+ "output_attribute_name_rank and output_attribute_name_heat "
										+ "where output_attribute_name can be set via the query "
										+ "string parameters (e.g., diffusion_output_rank and "
										+ "diffusion_output_heat). The _heat attribute will "
										+ "contain the heat of the node after diffusion. The _rank "
										+ "attribute will have the rank of the node relative to the "
										+ "heats of all other nodes in the network, starting with 0 "
										+ "as the hottest node.\n" + "\n"
										+ "Note that while _rank and _heat attributes "
										+ "will be returned for each node in the CX network, "
										+ "attributes present in the input network and not "
										+ "related to heat_diffusion are not guaranteed to be returned.",
								content = @Content(mediaType = MediaType.APPLICATION_JSON,
								schema = @Schema(implementation = CXMateResult.class))),
				   @ApiResponse(responseCode = "500",
						        description = "There was an error parsing input or running task",
				                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                  schema = @Schema(implementation = CXMateResult.class)))
			   })
	public Response legacyDiffusion(@RequestBody(description="The body of the request must be a CX network "
			+ "containing the nodes, edges, and nodeAttributes aspects. There must exist at least one "
			+ "nodeAttribute with a key name that matches the input_attribute_name parameter and holds "
			+ "a double, which will be interepreted as the heat of that node. (This condition can be "
			+ "minimally fulfilled by omitting the input_attribute_name parameter, and having at "
			+ "least one node with an attribute named diffusion_input with value 1.0.)\n" +
"\n" +
"All nodes that do not have this nodeAttribute set will be treated as having zero heat.", required = true) final String cxdata,
			@Parameter(description = "The upper bound on the exponential multiplication performed by diffusion", example="0.1") @QueryParam("time") Double time,
			@Parameter(description = "If True, will create a normalized laplacian matrix for diffusion") @QueryParam("normalize_laplacian") Boolean normalizeLaplacian,
			@Parameter(description = "The key diffusion will use to search for heats in the node attributes") @QueryParam("input_attribute_name") final String inputAttributeName,
			@Parameter(description = "Will be the prefix of the _rank and _heat attributes created by diffusion") @QueryParam("output_attribute_name") final String outputAttributeName) {
		
		
		ObjectMapper omapper = new ObjectMapper();
		// Create CommunityDetectionRequest passing cxdata in "data" field
		CommunityDetectionRequest cdr = new CommunityDetectionRequest();
		
		Map<String, String> customParams = new HashMap<>();
		if (time != null){
			customParams.put("--time", time.toString());
		}
		if (normalizeLaplacian != null){
			customParams.put("--normalize_laplacian", normalizeLaplacian.toString());
		}
		if (inputAttributeName != null){
			customParams.put("--input_attribute_name", inputAttributeName);
		}
		if (inputAttributeName != null){
			customParams.put("--output_attribute_name", outputAttributeName);
		}
		if (customParams.isEmpty() == false){
			cdr.setCustomParameters(customParams);
		}
		try {
			CommunityDetectionAlgorithm cda = Configuration.getInstance().getDiffusionAlgorithm();
			cdr.setAlgorithm(cda.getName());
			cdr.setData(omapper.readTree(cxdata));
			
			CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
				if (engine == null){
					throw new NullPointerException("CommunityDetection Engine not loaded");
				}
			// Submit task to service
			String id = engine.request(cdr);
			if (id == null){
					throw new CommunityDetectionException("No id returned from CommunityDetection engine");
			}
			// Wait for completion by invoking status once per second
			CommunityDetectionResult cRes = engine.getResult(id);
			while (cRes.getProgress() < 100){				
				Thread.sleep(100);
				cRes = engine.getResult(id);
			}
			if (cRes.getStatus().equals(CommunityDetectionResult.COMPLETE_STATUS)){
				
				return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
						.entity(omapper.writeValueAsString(cRes.getResult())).build();
			}
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(cRes.getResult()).build();
		} catch(JsonProcessingException jpe){
			CXMateResult er = new CXMateResult("Error parsing input data", cdr.getAlgorithm(),
			Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), jpe);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
		} catch(CommunityDetectionBadRequestException breq){
            ErrorResponse er = breq.getErrorResponse();
            if (er == null){
                er = new ErrorResponse("Bad request received", breq);
            }
			CXMateResult cError = new CXMateResult(cdr.getAlgorithm(), er);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(cError.asJson()).build();
		} catch(Exception ex){
			CXMateResult er = new CXMateResult("Error running diffusion", cdr.getAlgorithm(),
			Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
	}
    
    /**
     * Handles requests to do enrichment
     * @return {@link javax.ws.rs.core.Response} 
     */
    @POST 
    @Path(Configuration.V_ONE_PATH + "/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submits Community Detection task",
               description="Payload in JSON format needs to have data along with name of algorithm to run "
                       + "and any algorithm specific parameters. Information about what algorithms are available"
                       + "and what are the custom parameters can obtained by visiting the 'algorithms'"
                       + "endpoint \n" +
"\n" +
"The service should upon post return 202 and set location to resource to poll for result. Which will\n" +
"Match the URL of GET request below.",
               responses = {
                   @ApiResponse(responseCode = "202",
                           description = "The task was successfully submitted to the service. Visit the URL "
                                   + "specified in Location field in HEADERS to get status and results"
                                   + "In addition, the id(s) of the task(s) are returned as json\n",
                           headers = @Header(name = "Location", description = "URL containing resource generated by this request"),
                           content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = Task.class))),
                   @ApiResponse(responseCode = "400", description = "Bad Request",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class))),
                   @ApiResponse(responseCode = "500", description = "Server Error",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class)))
               })
    public Response request(@RequestBody(description="Request as json", required = true,
                                                   content = @Content(schema = @Schema(implementation = CommunityDetectionRequest.class))) final String query) {
        ObjectMapper omappy = new ObjectMapper();

        try {
            // not sure why but I cannot get resteasy and jackson to worktogether to
            // automatically translate json to Query class so I'm doing it after the
            // fact
            CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
            if (engine == null){
                throw new NullPointerException("CommunityDetection Engine not loaded");
            }
            CommunityDetectionRequest pQuery = omappy.readValue(query, CommunityDetectionRequest.class);
            String id = engine.request(pQuery);
            if (id == null){
                throw new CommunityDetectionException("No id returned from CommunityDetection engine");
            }
            Task t = new Task();
            t.setId(id);
            return Response.status(202).location(new URI(Configuration.getInstance().getHostURL() +
                                                         Configuration.V_ONE_PATH + "/" + id).normalize()).entity(omappy.writeValueAsString(t)).build();
        } catch(CommunityDetectionBadRequestException breq){
            ErrorResponse er = breq.getErrorResponse();
            if (er == null){
                er = new ErrorResponse("Bad request received", breq);
            }
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }catch(Exception ex){
            ErrorResponse er = new ErrorResponse("Error requesting CommunityDetection", ex);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
    }

    @GET 
    @Path(Configuration.V_ONE_PATH + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets result of Community Detection task",
               description="NOTE: For incomplete/failed jobs only Status, message, progress, and walltime will\n" +
"be returned in JSON",
               responses = {
                   @ApiResponse(responseCode = "200",
                           description = "Success",
                           content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = CommunityDetectionResult.class))),
                   @ApiResponse(responseCode = "410",
                           description = "Task not found"),
                   @ApiResponse(responseCode = "500", description = "Server Error",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class)))
               })
    public Response getResult(@PathParam("id") final String id) {
        ObjectMapper omappy = new ObjectMapper();

        try {
            CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
            if (engine == null){
                throw new NullPointerException("CommunityDetection Engine not loaded");
            }
            
            CommunityDetectionResult eqr = engine.getResult(id);
            if (eqr == null){
                return Response.status(410).build();
            }
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(omappy.writeValueAsString(eqr)).build();
        }
        catch(Exception ex){
            ErrorResponse er = new ErrorResponse("Error getting results for id: " + id, ex);
            return Response.status(500).type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
    }
    
    @GET
    @Path(Configuration.V_ONE_PATH + "/algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets list of algorithms supported by this service",
               description = "Provides detailed information about each algorithm/task that can be used with this service",
               responses = {@ApiResponse(responseCode = "200",
                           description = "Success",
                           content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = CommunityDetectionAlgorithms.class))),
                   @ApiResponse(responseCode = "410",
                           description = "Task not found"),
                   @ApiResponse(responseCode = "500", description = "Server Error",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class)))            
               })
    public Response getAlgorithms(){
        ObjectMapper omappy = new ObjectMapper();

        try {
            CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
            if (engine == null){
                throw new NullPointerException("CommunityDetection Engine not loaded");
            }
            
            CommunityDetectionAlgorithms cda = engine.getAlgorithms();
            if (cda == null){
                return Response.status(410).build();
            }
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(omappy.writeValueAsString(cda)).build();
        }
        catch(Exception ex){
            ErrorResponse er = new ErrorResponse("Error trying to get list of algorithms", ex);
            return Response.status(500).type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
    }

    @GET 
    @Path(Configuration.V_ONE_PATH + "/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets status of Community Detection task",
               description="This lets caller get status without getting the full result back",
               responses = {
                   @ApiResponse(responseCode = "200",
                           description = "Success",
                           content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = CommunityDetectionResultStatus.class))),
                   @ApiResponse(responseCode = "410",
                           description = "Task not found"),
                   @ApiResponse(responseCode = "500", description = "Server Error",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class)))
               })
    public Response getRequestStatus(@PathParam("id") final String id) {
        ObjectMapper omappy = new ObjectMapper();

        try {
            CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
            if (engine == null){
                throw new NullPointerException("CommunityDetection Engine not loaded");
            }
            CommunityDetectionResultStatus eqs = engine.getStatus(id);
            if (eqs ==  null){
                return Response.status(410).build();
            }
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(omappy.writeValueAsString(eqs)).build();
        }
        catch(Exception ex){
            ErrorResponse er = new ErrorResponse("Error getting results for id: " + id, ex);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
    }

    @DELETE 
    @Path(Configuration.V_ONE_PATH + "/{id}")
    @Operation(summary = "Deletes task associated with {id} passed in",
               description="",
               responses = {
                   @ApiResponse(responseCode = "200",
                           description = "Delete request successfully received"),
                   @ApiResponse(responseCode = "400",
                           description = "Invalid delete request"),
                   @ApiResponse(responseCode = "500", description = "Server Error",
                                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ErrorResponse.class)))
               })
    public Response deleteRequest(@PathParam("id") final String id) {
        ObjectMapper omappy = new ObjectMapper();

        try {
            CommunityDetectionEngine engine = Configuration.getInstance().getCommunityDetectionEngine();
            if (engine == null){
                throw new NullPointerException("CommunityDetection Engine not loaded");
            }
            engine.delete(id);
            return Response.ok().build();
        }
        catch(Exception ex){
            ErrorResponse er = new ErrorResponse("Error deleting: " + id, ex);
            return Response.serverError().type(MediaType.APPLICATION_JSON).entity(er.asJson()).build();
        }
    }
}