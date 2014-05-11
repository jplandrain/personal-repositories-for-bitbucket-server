package org.networkedassets.atlassian.stash.private_repositories_permissions.ao;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import net.java.ao.Query;

import org.networkedassets.atlassian.stash.private_repositories_permissions.service.AllowedUsersService;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.PageRequestImpl;

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
		User user = ao.create(User.class);
		user.setName(userName);
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
				Query.select().where("name = ?", userName));
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

		List<StashUser> filteredUsers = new ArrayList<StashUser>();

		for (StashUser stashUser : stashUsers) {
			for (User user : allowedUsers) {
				if (stashUser.getName() != user.getName()) {
					filteredUsers.add(stashUser);
				}
			}
		}

		return filteredUsers;
	}

}
