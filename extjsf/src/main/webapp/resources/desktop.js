/*===============================================================+
 |                                                     desktop   |
 |   Core Desktop Support Scripts for ExtJSF                     |
 |                                   / anton.baukin@gmail.com /  |
 +===============================================================*/


// +----: Desktop :----------------------------------------------+

/**
 * Desktop is a root controller of the entire page built
 * with Ext JS panels that are named regions. Each panel
 * has related top-bar extension panel where buttons or
 * menus are placed depending on the actual content
 * of the region panel.
 *
 * The main (and biggest) working region panel is named
 * as 'center'. It's top-bar has main application menu.
 */
extjsf.Desktop = ZeT.defineClass('extjsf.Desktop',
{
	className        : 'extjsf.Desktop',
	extjsfDesktop    : true,

	init             : function(opts)
	{
		this.opts = opts || {}

		//~: the panels of the desktop
		this.panels = {}

		//~: set collapsing strategy
		this.collapsing = this.opts.collapsing ||
		  ZeT.createInstance('extjsf.Desktop.Collapsing')

		//~: bind the desktop with the collapsing startegy
		this.collapsing.desktop(this)
	},

	/**
	 * Loads content of ExtJSF generated Binds
	 * by the URL wrapped with extjsf_go_url().
	 * See extjsf.Desktop.Load strategy.
	 */
	loadPanel        : function(url, opts)
	{
		opts = ZeT.extend(opts, {
		  desktop: this, url: url
		})

		//!: load via the strategy
		ZeT.createInstance('extjsf.Desktop.Load', opts).load()
	},

	/**
	 * Swaps the content of the panels given.
	 * If panel has no content, it just recieves
	 * one from the opposite panel of the swapped.
	 */
	swapPanels       : function(one, two)
	{
		ZeT.assert(one != two)

		var cone = this.panelController(one)
		var ctwo = this.panelController(two)

		//~: temporarily remove the contents of the panels
		if(cone) cone.remove(false)
		if(ctwo) ctwo.remove(false)

		//~: swap the positions
		if(cone) cone.position(two)
		if(ctwo) ctwo.position(one)

		//~: insert the contents back
		if(cone) cone.insert()
		if(ctwo) ctwo.insert()

		return this
	},

	/**
	 * Returns the controller of the panel defined
	 * by it's bind or the position.
	 */
	controller        : function(/* panel | position */)
	{
		var p = arguments[0]

		//?: {is a position}
		if(ZeT.iss(p)) p = this.panels[p]

		//?: {not a bind}
		ZeT.assert(extjsf.isbind(p))

		return ZeT.assertn(this.$data(p).controller)
	},

	/**
	 * Registers panel Bind in the desktop. Leading
	 * arguments are of extjsf.bind(), the last one
	 * is the panel position string.
	 */
	bindPanel        : function(/* ... position */)
	{
		var b = extjsf.bind.apply(extjsf, arguments)
		var p = arguments[arguments.length - 1]
		ZeT.asserts(p, 'Desktop panel position is empty!')

		//~: register the bind of the panel
		this.panels[p] = b

		//~: assign the panel controller
		this.$bind_controller(b, p)

		//~: hook the component creation
		b.when(ZeT.fbind(this.$on_panel_added, this, b, p))
	},

	/**
	 * Activates the desktop after installing
	 * it's panels. Invoked on Ext-ready.
	 */
	activate         : function()
	{
		var self = this

		ZeT.each(ZeT.keys(this.panels), function(k)
		{
			self.controller(k).triggerVoid()
		})

		return this
	},

	/**
	 * Registers or returns the controller of desktop panel
	 * inserted into the desktop region by the given position.
	 */
	panelController  : function(position)
	{
		//?: {wrong position}
		ZeT.asserts(position)

		//~: position -> controller
		var rc = this._root_controllers
		if(!rc) this._root_controllers = rc = {}

		//?: {return controller}
		if(arguments.length == 1)
			return rc[position]

		//?: {delete controller}
		var c = arguments[1]
		if(!c) delete rc[position]; else
		{
			ZeT.assert(c.desktopPanel === true)
			rc[position] = c
		}

		return this
	},

	/**
	 * Ready point is an extension point added to the desktop
	 * to allow various nested components to tell the desktop
	 * are they ready or not. When all the points are ready,
	 * desktop fires ready event. This allows client code
	 * to activate when all complex layout is ready.
	 *
	 * Note that the name of the point must be unique
	 * not to mess them. Ready argument must be a boolean
	 * (defaults to false).
	 *
	 * This is a Barrier pattern.
	 */
	readyPoint       : function(name, ready)
	{
		ZeT.asserts(name)
		if(ZeT.isu(ready)) ready = false
		ZeT.assert(ZeT.isb(ready))

		//~: ready points map on the first demand
		var rps = this._ready_points
		if(!rps) this._ready_points = rps = {}

		//~: add the point if it absents
		var rp = rps[name]
		if(rp) rp.ready = ready; else
			rps[name] = { name: name, ready: ready }

		//ZeT.log('Ready point [', name, ']: ', ready)

		//~: inspect whether all are ready
		var ready_go = true
		ZeT.each(rps, function(x)
		{
			if(x.ready) return
			ready_go = false
			return false
		})

		//?: {all are ready} go!
		if(this._ready_go = ready_go)
		{
			var fs = this._ready_fs

			//!: clear ready callbacks list
			delete this._ready_fs

			if(fs) fs.each(function(f)
			{
				try
				{
					f()
				}
				catch(e)
				{
					ZeT.log('Error in Desktop onReady() ',
					  'callback! \n', f, '\n', e)
				}
			})
		}

		return this
	},

	/**
	 * Adds Desktop callback waiting all ready
	 * point to become available. If Desktop
	 * is now ready, invokes the callback
	 * without adding it.
	 */
	onReady          : function(f)
	{
		ZeT.assertf(f)

		//?: {desktop is now ready}
		if(this._ready_go !== false)
			return f()

		//~: create callbacks on the first demand
		if(!this._ready_fs)
			this._ready_fs = new ZeT.Map()

		//~: add the callback
		this._ready_fs.put(f)

		return this
	},

	$data            : function(panel)
	{
		var d = panel.$desktop
		return (d)?(d):(panel.$desktop = {})
	},

	$bind_controller : function(panel, pos)
	{
		ZeT.assert(!this.$data(panel).controller)

		//~: create controller instance
		this.$data(panel).controller =
		  this.$new_controller(panel, pos)

		//~: bind the controller to this panel
		this.$data(panel).controller.panel(panel)
	},

	$new_controller  : function(panel, pos)
	{
		return ZeT.createInstance(
		  'extjsf.Desktop.Region', {
		  desktop: this, position: pos
		})
	},

	$on_panel_added  : function(panel, pos)
	{
		this.collapsing.onPanelAdded(panel, pos)
	}
})


// +----: Desktop Collapsing :------------------------------------+

/**
 * Strategy that handles collapsing of a Desktop panels.
 * Note that strategy instance is assigned to a Desktop
 * instance, but not to instance of a panel (Bind).
 * Effectively, this strategy is a Singleton.
 */
ZeT.defineClass('extjsf.Desktop.Collapsing',
{
	className        : 'extjsf.Desktop.Collapsing',

	init             : function(opts)
	{
		this.opts = opts || {}
	},

	desktop          : function(desktop)
	{
		if(!desktop) return this.desktop
		this.desktop = desktop
		return this
	},

	onPanelAdded     : function(panel, position)
	{
		if(!extjsf.isbind(panel))
			panel = extjsf.bind(panel)

		ZeT.assert(extjsf.isbind(panel),
		  'Desktop panel has no ExtJSF Bind!')

		//?: {empty position}
		ZeT.asserts(position,
		  'Desktop panel has no position!')

		this.$bind_panel(panel, position)
	},

	$data            : function(panel)
	{
		if(!panel.$desktop) panel.$desktop = {}
		var d = panel.$desktop.collapsing
		return (d)?(d):(panel.$desktop.collapsing = {})
	},

	$bind_panel      : function(panel, pos)
	{
		var self = this; function mbind(m)
		{
			return ZeT.fbind(m, self, panel)
		}

		//=: place collapsing data in the bind
		ZeT.extend(this.$data(panel), { position: pos,
		  click: mbind(self.$collapsed_click)
		})

		//~: react on desktop panel events
		panel.on({
		  beforecollapse : mbind(self.$before_collapse),
		  collapse       : mbind(self.$after_collapse),
		  beforeexpand   : mbind(self.$before_expand),
		  expand         : mbind(self.$after_expand),
		  afterlayout    : mbind(self.$after_layout)
		})
	},

	$collapsed_click : function(panel)
	{
		panel.co().expand()
	},

	$before_collapse : function(panel)
	{
		var d = this.$data(panel)
		var w = panel.co().getWidth()
		var W = panel.co().ownerCt.getWidth()

		//~: original width is relative
		d.originalWidth = (w / W)
		d.setOriginalWidth = true
	},

	$after_collapse  : function(panel)
	{
		var self = this; ZeT.timeout(500, function()
		{
			var e = panel.co().getEl()
			var c = self.$data(panel).click

			e.removeListener('click', c)
			e.addListener('click', c)
		})
	},

	$before_expand   : function(panel)
	{
		panel.co().getEl().removeListener(
		  'click', this.$data(panel).click)
	},

	$after_expand    : function(panel)
	{},

	$after_layout    : function(panel)
	{
		if(!this.$data(panel).setOriginalWidth)
			return

		var d = this.$data(panel)
		var W = panel.co().ownerCt.getWidth()
		var w = Math.round(W * d.originalWidth)

		//!: prevents infinite recursion
		delete d.setOriginalWidth
		panel.co().setWidth(w)
	}
})


// +----: Desktop Panel Controller :------------------------------+

/**
 * Controller of a Desktop region panel. Instances
 * are created by the Desktop when adding panels.
 */
ZeT.defineClass('extjsf.Desktop.Region',
{
	className        : 'extjsf.Desktop.Region',
	desktopRegion    : true,

	init             : function(opts)
	{
		ZeT.assert(ZeT.iso(opts))
		this.opts = opts
	},

	/**
	 * Returns or alters the position of the panel.
	 */
	position         : function(pos)
	{
		if(!arguments.length)
			return this.opts.position

		this.opts.position = ZeT.asserts(pos)
		return this
	},

	/**
	 * Assigns or returns Bind of the panel.
	 */
	panel            : function(panel)
	{
		if(!arguments.length)
			return this._root_bind

		ZeT.assert(extjsf.isbind(panel))
		this._panel = panel
		return this
	},

	/**
	 * Assigns or returns Bind of the extension
	 * panel with control buttons and menus.
	 * Arguments are of extjsf.bind().
	 */
	mainTopbar       : function(/* ... */)
	{
		if(!arguments.length)
			return this._main_topbar

		this._main_topbar = ZeT.assertn(
		  extjsf.bind.apply(extjsf, arguments))

		return this
	},

	/**
	 * Binds processor of inserting-removing the main
	 * menu components dependend on the panel contents.
	 * (Extension point for the main menus.)
	 *
	 * Callback function has the following arguments:
	 *
	 *  0) inserting flag (true), or removing (false);
	 *
	 *  1) callback function of the component that
	 *     implements the required action (inserts,
	 *     or removes);
	 *
	 *  2) controller of the region panel.
	 */
	mainMenuProc     : function(f)
	{
		if(ZeT.isu(f))
			return this._main_menu_proc

		this._main_menu_proc = ZeT.assertf(f)
		return this
	},

	/**
	 * Extension point to add root panel controls
	 * onto the top bar. Arguments are of extjsf.bind().
	 */
	mainTopbarExt    : function(/* ... */)
	{
		if(!arguments.length)
			return this._main_topbar_ext

		this._main_topbar_ext = ZeT.assertn(
		  extjsf.bind.apply(extjsf, arguments))

		return this
	},

	/**
	 * The panel to insert the content components
	 * of the panel. It usually has fit layout.
	 * Arguments are of extjsf.bind().
	 */
	contentPanel     : function(/* ... */)
	{
		if(!arguments.length)
			return this._content_panel

		this._content_panel = ZeT.assertn(
		  extjsf.bind.apply(extjsf, arguments))

		return this
	},

	/**
	 * If set, void panel is displayed when
	 * content panel is empty. Arguments are
	 * of extjsf.bind().
	 */
	voidPanel        : function(/* ... */)
	{
		if(!arguments.length)
			return this._void_panel

		this._void_panel = ZeT.assertn(
		  extjsf.bind.apply(extjsf, arguments))

		return this
	},

	/**
	 * Tells whether to show void panel: else panels
	 * are empty and void panel is assigned.
	 */
	isVoid           : function()
	{
		if(!this._void_panel)
			return undefined

		if(!this._void_panel.co())
			return undefined

		if(this._main_topbar)
		{
			var c = this._main_topbar.co()
			if(c && c.items.getCount()) return false
		}

		if(this._main_topbar_ext)
		{
			c = this._main_topbar_ext.co()
			if(c && c.items.getCount()) return false
		}

		if(this._content_panel)
		{
			c = this._content_panel.co()
			if(c && c.items.getCount()) return false
		}

		return true
	},

	/**
	 * Checks whether the panel is void and
	 * hides all content except the void panel.
	 */
	triggerVoid      : function()
	{
		//?: {has no void panel}
		var vo = this.isVoid()
		if(ZeT.isu(vo)) return

		//~: show-hide void panel
		this._void_panel.visible(vo)

		//~: hide-show all else panels
		ZeT.each(this.$all_panes(),
		  function(p) { p && p.visible(!vo) })
	},

	$all_panes       : function()
	{
		return [
		  this._main_topbar,
		  this._main_topbar_ext,
		  this._content_panel
		]
	}
})


// +----: Desktop Panel :----------------------------------------+

/**
 * Each root panel' Bind has has an instance of this class
 * as 'desktopPanelController' property. It is to control
 * the behaviour of the panel on the desktop.
 * See x:desktop-panel component.
 */
ZeT.defineClass('extjsf.Desktop.Panel',
{
	className        : 'extjsf.Desktop.Panel',
	desktopPanel     : true,

	/**
	 * Required configuration options are:
	 *
	 * · domain   name of ExtJSF domain;
	 *
	 * · desktop  Desktop instance;
	 *
	 * · bind     Bind of the panel. If is given as object,
	 *            domain option is not required;
	 *
	 * · position name of the position; one of the desktop
	 *            region panels name.
	 *
	 *
	 * Additional options are:
	 *
	 * · nomove   tells not to create header tools to move
	 *            the content between the regions.
	 */
	init             : function(opts)
	{
		ZeT.assert(ZeT.iso(opts))
		this.opts = opts

		//~: check the root-panel bind
		this.bind()

		//~: check domain
		this.domain()

		//~: check desktop
		this.desktop()

		//~: check the position
		this.position()

		opts.bind.desktopPanelController = this

		//~: clear content on destroy
		this.bind().on('beforedestroy',
		  ZeT.fbind(this.$destroy, this)
		)
	},

	/**
	 * Returns name of ExtJSF Domain.
	 */
	domain           : function()
	{
		var d = this.opts.domain

		//?: {find domain in the bind}
		if(ZeT.ises(d) && extjsf.isbind(this.opts.bind))
			d = this.opts.bind.domain

		return ZeT.asserts(d, 'No Domain name specified!')
	},

	desktop          : function()
	{
		var d = this.opts.desktop

		ZeT.assert(d && (d.extjsfDesktop === true),
		  'No valid Desktop instance is defined!')

		return d
	},

	/**
	 * Returns Bind of the panel this
	 * controller is created for.
	 */
	bind             : function()
	{
		var b = this.opts.bind

		if(ZeT.iss(b)) //?: {lookup by name}
			b = extjsf.bind(b, this.domain())

		ZeT.assert(extjsf.isbind(b),
		  'Desktop panel Bind instance not found!')

		return b
	},

	/**
	 * Assigns or returns the name of the desktop
	 * region panel this panel is inserted in
	 * as a content.
	 */
	position         : function()
	{
		if(!arguments.length)
			//?: {the position key is undefined}
			return ZeT.asserts(this.opts.position,
			  'No Desktop region position is defined!')

		//=: set the position option
		this.opts.position = ZeT.asserts(arguments[0])

		//~: check the controller
		this.regionController()

		return this
	},

	/**
	 * Returns controller of the related Desktop
	 * region panel, instance of extjsf.Desktop.Region.
	 */
	regionController : function()
	{
		var pos = this.opts.position

		//?: {no panel controller at that position}
		return ZeT.assertn(this.desktop().controller(pos),
		  'Desktop panel controller at position [',
		   pos, '] was not found!')
	},

	topbarItems      : function(items)
	{
		if(!items) return this._topbar_items
		this._topbar_items = items
		return this
	},

	/**
	 * Takes two forms. First, when the first argument
	 * is boolean: returns insert (true), or remove (false)
	 * strategy of extending the main menu.
	 *
	 * Second, two arguments, both functions: to insert
	 * and to remove the menus. Installed by the root panel.
	 *
	 * Inserting and removing strategies are invoked
	 * with arguments depending on the implementation
	 * of the main menu. For menus on nodes with CSS
	 * (not of Ext JS) the first argument is menu node.
	 *
	 * If strategy returns false, menu is not supported
	 * for the current region panel.
	 */
	topbarMenu       : function()
	{
		var a0 = arguments[0]

		if(arguments.length == 2)
		{
			var i = a0, r = arguments[1]

			ZeT.assert(ZeT.isf(i) && ZeT.isf(r))
			this._topbar_menu = { insert: i, remove: r }

			return this
		}

		ZeT.assert(arguments.length == 1)
		ZeT.assert(ZeT.isb(a0))

		if(!this._topbar_menu) return undefined
		return this._topbar_menu[(a0)?('insert'):('remove')]
	},

	toolbar          : function(bind)
	{
		if(!bind) return this._toolbar
		this._toolbar = bind
		return this
	},

	status           : function(bind)
	{
		if(!bind) return this._status
		this._status = bind
		return this
	},

	/**
	 * Inserts the controls of this panel into the Desktop.
	 * The region panel to insert is defined by the position.
	 */
	insert           : function()
	{
		//~: register this controller
		this.$register()

		//~: set the tools of root-panel
		this.$set_tools()

		//~: insert the top bar controls
		this.$insert_topbar()

		//~: insert the main content
		this.$insert_content()

		//~: hide void panel
		this.regionController().triggerVoid()
	},

	/**
	 * Removes the panel from the region panel of the Desktop
	 * it was previously inserted in via insert().
	 *
	 * Optional destroy argument (false by default)
	 * tells whether to destroy the panel and
	 * it's domain (if it's domain owner).
	 */
	remove           : function(destroy)
	{
		//~: remove the main content
		this.$remove_content(destroy)

		//~: remove the top bar controls
		this.$remove_topbar(destroy)

		//~: clear registration of this controller
		this.$unregister()

		//~: show void panel
		var rc = this.regionController()
		ZeT.timeout(100, ZeT.fbind(rc.triggerVoid, rc))
	},

	$destroy         : function()
	{
		this.remove(false)
	},

	$register        : function()
	{
		this.desktop().panelController(this.position(), this)
	},

	$unregister      : function()
	{
		this.desktop().panelController(this.position(), null)
	},

	$set_tools       : function()
	{
		if(this.opts['notools'] === true) return
		if(this._tools_are_set) return
		this._tools_are_set = true

		var tools = this.bind().$raw().tools
		if(!ZeT.isa(tools)) tools = []

		//~: add the tools of the panel
		this.$add_tools(tools)

		if(tools.length)
			this.bind().props({'tools': tools})
	},

	$add_tools       : function(tools)
	{
		//~: add tool to save web link
		if(ZeT.isox(this.opts.webLink))
			this.$add_link_tool(tools)

		//~: add panel move left-right tools
		this.$add_move_tools(tools)
	},

	/**
	 * Override this method to add tool to create
	 * persistent link to this panel in the header
	 * of the bound panel.
	 */
	$add_link_tool   : function(tools)
	{},

	$add_move_tools  : function(tools)
	{
		if(this.opts['nomove'] === true) return

		//~: add move <<
		tools.push(this.$new_move_tool('left'))

		var m = extjsf.pts(0, 0, 0, 2)
		if(this.bind().$raw()['closable'])
			m = extjsf.pts(0, 8, 0, 2)

		//~: add move >>
		tools.push(this.$new_move_tool('right'))
	},

	$move_tips       : {
	  left  : 'Передвинуть панель в левую область',
	  right : 'Передвинуть панель в правую область'
	},

	$new_move_tool   : function(dir)
	{
		return { xtype: 'tool', type: dir,
		  handler: ZeT.fbind(this['$move_' + dir], this),
		  margin: extjsf.pts(0, 8, 0, 2), tooltipType: 'title',
		  tooltip: this.$move_tips[dir]
		}
	},

	$insert_content  : function()
	{
		//~: add the removed content nodes
		this.$insert_nodes(this.$insert_target(),
		  '_removed_content_nodes')

		//~: insert the components
		this.$insert_co()
	},

	$insert_target   : function()
	{
		var cnt = this.regionController().contentPanel()

		ZeT.assert(extjsf.bind(cnt) && cnt.co(),
		  'Content panel of region [', this.position(),
		  'is not yet ready!')

		return cnt
	},

	/**
	 * Private. Inserts the components of panel.
	 */
	$insert_co       : function()
	{
		var cnt = this.$insert_target()

		//~: add the component
		cnt.co().add(this.bind().co(true))

		//?: {has toolbar} dock it
		this.$insert_docked(this.toolbar())

		//?: {has status bar} dock it
		this.$insert_docked(this.status())
	},

	/**
	 * Private. Docks component (of the bind)
	 * to the content panel.
	 */
	$insert_docked   : function(bind)
	{
		var co = bind && bind.co(true)
		if(!co) return

		var ds = this.bind().co().getDockedItems()

		//?: {has no component docked}
		if(ds && (ds.indexOf(co) == -1))
			this.bind().co().addDocked(co)
	},

	/**
	 * Private. Each ExtJSF component generates
	 * additional DOM nodes for configuration facests
	 * that are also inserted to the content panels
	 * of the regions. This function inserts them back,
	 * is being previously removed.
	 */
	$insert_nodes    : function(cnt, property)
	{
		if(!cnt || !cnt.co()) return

		//~: take the nodes previously removed
		var nodes = this[property]
		if(!ZeT.isa(nodes)) return
		delete this[property]

		//~: get the body of the panel
		var body = ZeT.assertn(cnt.co().
		  getEl().down('.x-panel-body'))

		//~: add the nodes previously removed
		for(var i = 0;(i < nodes.length);i++)
			body.dom.appendChild(nodes[i])
	},

	$remove_content  : function(destroy)
	{
		//~: remove the component
		var cnt = this.$insert_target()
		cnt.co().remove(this.bind().co(), !!destroy)

		//~: remove content nodes
		this.$remove_nodes(cnt, destroy,
		  '_removed_content_nodes')
	},

	/**
	 * Private. Removes nodes from the content panel
	 * saving them by the property as $insert_nodes().
	 */
	$remove_nodes    : function(cnt, destroy, property)
	{
		if(!cnt || !cnt.co()) return

		//~: get the body of the panel
		var body = ZeT.assertn(cnt.co().
		  getEl().down('.x-panel-body'))

		//~: collect all the nodes
		var n, x = body.dom.firstChild
		var a = []; while(n = x)
		{
			a.push(n); x = n.nextSibling
			body.dom.removeChild(n)
		}

		if(!destroy) this[property] = a
	},

	$insert_topbar   : function()
	{
		//?: {inserted in main menu}
		if(this.$topbar_menu(true)) return

		var tb = this.regionController().mainTopbarExt()

		//~: add the removed content nodes
		this.$insert_nodes(tb,
		  '_removed_topbar_nodes')

		//~: insert previously removed nodes
		this.$insert_items(tb, this._topbar_items)
	},

	$insert_items    : function(ext, items)
	{
		//?: {no extension point bind}
		if(!ext || !ext.co()) return

		ZeT.each(items, function(item)
		{
			if(!ext.co().contains(item.co(true)))
				ext.co().add(item.co())
		})
	},

	/**
	 * Inserts or removes menu items to the top-bar
	 * menu (if it's supported) of current region panel.
	 *
	 * When this function returns true, additional
	 * controls are not inserted into the extension
	 * panel. This supports top-bar of center region
	 * when it has menu only (also, not of Ext JS).
	 */
	$topbar_menu     : function(insert)
	{
		var rc = this.regionController()

		//?: {no main menu processor}
		var f; if(!(f = rc.mainMenuProc())) return

		//?: {has no top-bar menu}
		var m; if(!(m = this.topbarMenu(insert))) return

		//!: invoke the menu processor
		return !f(insert, m, rc)
	},

	$remove_topbar   : function(destroy)
	{
		//?: {removed from main menu}
		if(this.$topbar_menu(false)) return

		var tb = this.regionController().mainTopbarExt()

		//~: insert previously removed nodes
		this.$remove_items(tb, this._topbar_items, destroy)

		//~: add the removed content nodes
		this.$remove_nodes(tb, destroy,
		  '_removed_topbar_nodes')
	},

	$remove_items    : function(ext, items, destroy)
	{
		//?: {no extension point bind}
		if(!ext || !ext.co()) return

		ZeT.each(items, function(item)
		{
			if(item.co())
				ext.co().remove(item.co(), !!destroy)
		})
	},

	$move_sequence   : [ 'left', 'center', 'right' ],

	$move_step       : function(pos, di)
	{
		var s = this.$move_sequence

		var i = s.indexOf(pos)
		ZeT.assert(i >= 0)

		if((i += di) < 0) i += s.length
		return s[i % s.length]
	},

	$move_left       : function()
	{
		var p = this.position()
		var n = this.$move_step(p, -1)

		ZeT.assert(p != n)
		this.desktop().swapPanels(p, n)
	},

	$move_right      : function()
	{
		var p = this.position()
		var n = this.$move_step(p, +1)

		ZeT.assert(p != n)
		this.desktop().swapPanels(p, n)
	}
})


// +----: Desktop Loader :---------------------------------------+

/**
 * Strategy that extends component loader to load
 * JSF pages generating components for Desktop
 * region content panels.
 *
 * In options required 'desktop' refers the Desktop.
 * Required option 'url' tells the requested address
 * of the XHTML-page that generated ExtJSF binds.
 *
 * The parameters option 'params' may have:
 *
 * · view   ExtJSF view id;
 * · mode   ExtJSF page mode (defaults to 'body').
 */
ZeT.defineClass('extjsf.Desktop.Load', extjsf.LoadCo,
{
	init             : function()
	{
		this.$applySuper(arguments)

		//~: check desktop
		this.desktop()

		//~: check the url
		this.$url()
	},

	desktop          : function()
	{
		var d = this.opts.desktop

		ZeT.assert(d && d.extjsfDesktop,
		  'No Desktop instance is given!')

		return d
	},

	/**
	 * Returns domain string from the options.
	 * Creates default domain name, if not given.
	 * Wraps result with extjsf.nameDomain().
	 */
	domain           : function()
	{
		if(this._domain)
			return this._domain

		var d = this.opts.domain

		//?: {take the default name}
		if(ZeT.ises(d))
			d = 'desktop:panel'

		//?: {add desktop prefix}
		if(!ZeT.ii(d, ':desktop', 'desktop:'))
			d = 'desktop:' + d

		return this._domain =
		  extjsf.nameDomain(d)
	},

	/**
	 * Returns desktop region position from the options
	 * having 'center' as the default value.
	 */
	position         : function()
	{
		var p = this.opts.position
		return ZeT.ises(p)?('center'):(p)
	},

	/**
	 * Component to load to is content panel of
	 * destop region panel in the position().
	 */
	co               : function()
	{
		var p = this.position(), b = ZeT.assertn(
		  this.desktop().controller(p).contentPanel(),
		  'Not found Desktop content panel ', p, ']!'
		)

		return b.co()
	},

	/**
	 * Override this method to add URL infix specific
	 * to the layout of XHTML-files in your application.
	 * This version just add prefix '/go/' that defaults
	 * to the go-dispatching servlet.
	 */
	$url             : function()
	{
		var url = ZeT.asserts(this.opts.url,
		  'URL of the Desktop panel content is not set!')

		//HINT: see scripts.xhtml page
		return extjsf_go_url(url)
	},

	$is_clear_domain : function()
	{
		return false
	},

	$is_clear_co     : function()
	{
		return !!this.desktop().
		  panelController(this.position())
	},

	$clear_co        : function()
	{
		this.desktop().panelController(
		  this.position()).remove()
	},

	$special_params  : function(ps)
	{
		this.$applySuper(arguments)

		//~: set the view id parameter
		if(!ps.view && !ZeT.ises(this.opts.view))
			ps.view = this.opts.view

		//~: set the view mode parameter (defaults to body)
		if(!ps.mode) ps.mode = ZeT.iss(this.opts.mode)?
		  (this.opts.mode):('body')

		//~: set the position parameter
		ps['desktop-position'] = this.position()
	}
})


// +----: Desktop Instance :-------------------------------------+

//extjsf.desktop = ZeT.defineInstance(
//  'extjsf.desktop', extjsf.Desktop)