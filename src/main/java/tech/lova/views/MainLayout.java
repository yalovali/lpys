package tech.lova.views;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

import tech.lova.data.User;
import tech.lova.security.AuthenticatedUser;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

	private H1 viewTitle;

	private AuthenticatedUser authenticatedUser;
	private AccessAnnotationChecker accessChecker;

	public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;

		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	private void addDrawerContent() {
		Span appName = new Span("LPYS");
		appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
		Header header = new Header(appName);

		Scroller scroller = new Scroller(createSideNav());

		addToDrawer(header, scroller, createFooter());
	}

	private void addHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.setAriaLabel("Menu toggle");

		viewTitle = new H1();
		viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

		addToNavbar(true, toggle, viewTitle);
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private Footer createFooter() {
		Footer layout = new Footer();

		Optional<User> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			User user = maybeUser.get();

			Avatar avatar = new Avatar(user.getName());
			StreamResource resource = new StreamResource("profile-pic",
					() -> new ByteArrayInputStream(user.getProfilePicture()));
			avatar.setImageResource(resource);
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			MenuBar userMenu = new MenuBar();
			userMenu.setThemeName("tertiary-inline contrast");

			MenuItem userName = userMenu.addItem("");
			Div div = new Div();
			div.add(avatar);
			div.add(user.getName());
			div.add(new Icon("lumo", "dropdown"));
			div.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL);
			userName.add(div);
			userName.getSubMenu().addItem("Sign out", e -> {
				authenticatedUser.logout();
			});

			layout.add(userMenu);
		}
		else {
			Anchor loginLink = new Anchor("login", "Sign in");
			layout.add(loginLink);
		}

		return layout;
	}

	/**
	 * Creates the side navigation menu.
	 *
	 * The navigation menu is dynamically populated with menu entries from
	 * `MenuConfiguration`. Each entry is represented as a `SideNavItem` with
	 * optional icons.
	 *
	 * @return A `SideNav` component containing the navigation items.
	 */
	private SideNav createSideNav() {
		var nav = new SideNav(); // Create the side navigation
		nav.addClassNames(Margin.Horizontal.MEDIUM); // Style the navigation
		MenuConfiguration.getMenuEntries().forEach(entry -> createSideNavItem(nav, entry)); // Add menu
																							// entries
		return nav;
	}

	/**
	 * Creates a side navigation item for a given menu entry.
	 *
	 * Each menu entry is represented as a `SideNavItem` with optional icons.
	 *
	 * @param menuEntry The menu entry to create a navigation item for.
	 * @return A `SideNavItem` representing the menu entry.
	 */
	private void createSideNavItem(SideNav nav, MenuEntry menuEntry) {
		if (menuEntry == null)
			return; // Return null if the menu entry is null
		// read the menu entry properties
		String title = menuEntry.title();
		String path = menuEntry.path();
		String icon = menuEntry.icon();
		// if title contains a dot, it is a sub-menu entry
		if (title.contains(".")) {
			var parts = title.split("\\.");
			title = parts[parts.length - 1]; // Use the last part as the title
			String parent_title = parts[0]; // Use the first part as the parent title
			// find the parent menu entry
			SideNavItem parentItem = nav.getItems().stream().filter(item -> item.getLabel().equals(parent_title))
					.findFirst().orElse(null);
			if (parentItem == null) {
				parentItem = new SideNavItem(parent_title);
				parentItem.setPrefixComponent(new Icon(icon)); // Set the icon for the parent item
				nav.addItem(parentItem); // Add the parent item to the navigation
			}
			// Create a sub-menu item under the parent entry
			parentItem.addItem(new SideNavItem(title, path, new Icon(icon)));
		}
		else
			// Create a top-level menu item
			nav.addItem(new SideNavItem(title, path, new Icon(icon))); // Create item with
	}

	private String getCurrentPageTitle() {
		return MenuConfiguration.getPageHeader(getContent()).orElse("");
	}
}
