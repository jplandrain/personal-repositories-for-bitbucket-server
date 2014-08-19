package org.networkedassets.atlassian.stash.privaterepos.user.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.networkedassets.atlassian.stash.privaterepos.auth.AdminAuthorizationVerifier;
import org.networkedassets.atlassian.stash.privaterepos.user.AllowedUsersService;
import org.networkedassets.atlassian.stash.privaterepos.util.rest.NamesList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/users/")
@Produces({ MediaType.APPLICATION_JSON })
public class UserRestService {

	@Autowired
	private AllowedUsersService allowedUsersService;
	@Autowired
	private UsersStateCreator usersStateCreator;
	@Autowired
	private AdminAuthorizationVerifier authorizationVerifier;

	@Path("find/{key}")
	@GET
	public List<UserState> findUsers(@PathParam("key") String key) {
		this.authorizationVerifier.verify();
		return usersStateCreator.createFrom(allowedUsersService.findNotAllowed(key));
	}

	@Path("list")
	@GET
	public List<UserState> getUsers() {
		this.authorizationVerifier.verify();
		return usersStateCreator.createFrom(allowedUsersService
				.getStashUsersFromUsers(allowedUsersService.all()));
	}

	@Path("list")
	@POST
	public Response addGroups(NamesList names) {
		this.authorizationVerifier.verify();
		this.allowedUsersService.allow(names.getNames());
		return Response.ok().build();
	}

	@Path("user/{user}")
	@POST
	public Response addUser(@Context UriInfo uriInfo,
			@PathParam("user") String userName) {
		this.authorizationVerifier.verify();
		allowedUsersService.allow(userName);
		return Response.created(uriInfo.getAbsolutePath()).build();
	}

	@Path("user/{user}")
	@DELETE
	public Response deleteUser(@PathParam("user") String userName) {
		this.authorizationVerifier.verify();
		allowedUsersService.disallow(userName);
		return Response.ok().build();
	}

}
