package org.networkedassets.atlassian.stash.privaterepos.user;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.java.ao.Query;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.PageRequestImpl;

@Component
public class AoAllowedUsersService implements AllowedUsersService {

	private final static int MAX_FOUND_USERS = 20;

	private final ActiveObjects ao;

	private final UserService userService;

	public AoAllowedUsersService(ActiveObjects ao, UserService userService) {
		this.userService = userService;
		this.ao = checkNotNull(ao);
	}

	@Override
	public List<User> all() {
		return newArrayList(ao.find(User.class, Query.select()));
	}

	@Override
	public User allow(String userName) {
		return addUser(userName);
	}
	
	@Override
	public List<User> allow(List<String> userNames) {
		List<User> users = new ArrayList<User>();
		for (String name : userNames) {
			users.add(addUser(name));
		}
		return users;
	}
	
	private User addUser(String name) {
		User user = ao.create(User.class);
		user.setName(name);
		user.save();
		return user;
	}

	@Override
	public void disallow(String userName) {
		User user = findUser(userName);
		if (user != null) {
			ao.delete(user);
		}
	}

	@Override
	public boolean isAllowed(String userName) {
		return findUser(userName) != null;
	}

	private User findUser(String userName) {
		User[] users = ao.find(User.class,
				Query.select().where("NAME = ?", userName));
		if (users.length == 0) {
			return null;
		} else {
			return users[0];
		}
	}

	@Override
	public List<StashUser> findNotAllowed(String key) {
		List<User> allowedUsers = all();
		Iterable<? extends StashUser> stashUsers = userService.findUsersByName(
				key, new PageRequestImpl(0, MAX_FOUND_USERS)).getValues();

		Map<String, StashUser> usersMap = new HashMap<String, StashUser>();
		
		for (StashUser stashUser : stashUsers) {
			usersMap.put(stashUser.getName(), stashUser);
		}
		
		for (User user : allowedUsers) {
			usersMap.remove(user.getName());
		}

		List<StashUser> filtered = new ArrayList<StashUser>();
		filtered.addAll(usersMap.values());
		
		return filtered;
	}

}
