/*===============================================================+
 |                                                     extjsf    |
 |   Ext JS for JavaServer Faces                                 |
 |                                   / anton.baukin@gmail.com /  |
 +===============================================================*/


var extjsf = ZeT.define('extjsf', {})


// +----: Domain :-----------------------------------------------+

/**
 * Domain of ExtJSF Binds. Each domain has unique name
 * that is empty string for the default global one.
 */
extjsf.Domain = ZeT.defineClass('extjsf.Domain',
{
	className        : 'extjsf.Domain',
	extjsfDomain     : true,

	init             : function(name)
	{
		//=: domain name
		ZeT.assert(ZeT.iss(name))
		this.name = name

		//=: domain binds
		this.binds = new ZeT.Map()

		//=: destroy listeners
		this.ondestr = new ZeT.Map()
	},

	/**
	 * Returns the Bind registered by the name that
	 * is unique within a Domain. Optional argument
	 * allows to register a Bind instance. Error is
	 * raised when a Bind is already registered.
	 */
	bind             : function(name /*, bind */)
	{
		ZeT.asserts(name, 'Can not define a Bind by ',
		  'not a string name: ', name)

		if(arguments.length == 1)
			return this.binds.get(name)
		ZeT.assert(arguments.length == 2)

		//~: bind argument
		var bind = arguments[1]
		ZeT.assert(bind.extjsfBind === true)

		//?: {bind with this name is already defined}
		ZeT.assert(!this.binds.contains(name), 'Bind [', name,
		  '] is already defined in Domain [', this.name, ']!')

		//~: register the bind
		this.binds.put(name, bind)
		bind.defined(this.name, name)

		return bind
	},

	/**
	 * Returns the Bind previously registered and
	 * clears it's registration. If second argument
	 * is not false, destroys the Bind. Note that
	 * destruction of a Bind does not mean it's
	 * Ext JS component is being destroyed!
	 */
	unbind           : function(bind, destroy)
	{
		//?: {bind instance}
		if(extjsf.isbind(bind))
			bind = bind.name

		//?: {not a string name}
		ZeT.asserts(bind, 'Can not lookup a Bind by ',
		  'not a string name: ', bind)

		//~: remove the bind
		var bind = this.binds.remove(bind)
		if(!bind) return

		//?: {do not destroy}
		if(destroy === false)
			return bind

		//!: do destroy
		bind.destroy()

		return bind
	},

	/**
	 * Destroys all currently registered Binds of
	 * the Domain in the order reversed to the
	 * registration one.
	 *
	 * Invokes all the Domain deletion callbacks
	 * currently registered in the order of the
	 * registration as: callback(domain).
	 *
	 * Additional options may be given: they are
	 * passed to each callback and destroy call.
	 */
	destroy          : function(opts)
	{
		var self = this

		//c: invoke the callbacks
		this.ondestr.each(function(f)
		{
			try
			{
				f(self, opts)
			}
			catch(e)
			{
				ZeT.log('Error in callback while deleting Domain [',
				  self.name, ']:\n', e)
			}
		})

		//c: destroy the binds
		this.binds.reverse(function(bind, name)
		{
			try
			{
				bind.destroy(opts)
			}
			catch(e)
			{
				ZeT.log('Error destroying Bind [', name, ']:\n', e)
			}
		})

		//~: clean-up
		this.binds.clear()
		this.ondestr.clear()
	},

	/**
	 * Registers a callback to invoke when Domain
	 * is being destroyed. Give second argument false
	 * to remove previously registered callback.
	 */
	onDestroy        : function(f, remove)
	{
		ZeT.assertf(f)

		if(remove === false)
			this.ondestr.remove(f)
		else
			this.ondestr.put(f)

		return this
	}
})


ZeT.extend(extjsf,
{
	/**
	 * Creates ExtJSF Domain on the first demand and
	 * registers it globally by the string name.
	 *
	 * Note that ''-empty name means the root Domain
	 * of the entire web page that is never destroyed.
	 */
	domain           : function(name)
	{
		ZeT.assert(ZeT.iss(name),
		  'Can not defined Domain by not a string name!')

		//~: global registry of the domains
		extjsf.domains = ZeT.define('extjsf.Domain.registry', {})

		//~: lookup there
		var domain = extjsf.domains[name]
		if(domain) return domain

		//~: create it
		extjsf.domains[name] = domain = new extjsf.Domain(name)

		//!: un-register on the destruction
		domain.onDestroy(function()
		{
			delete extjsf.domains[name]
		})

		return domain
	},

	/**
	 * Returns Bind registered in the Domain.
	 */
	bind             : function(name, domain)
	{
		if(ZeT.iss(domain))
			domain = extjsf.domain(domain)

		//?: {domain is not found}
		if(ZeT.isu(domain)) return

		ZeT.assert(domain.extjsfDomain === true)
		return domain.bind(name)
	}
})


// +----: Bind :-------------------------------------------------+

extjsf.Bind = ZeT.defineClass('extjsf.Bind',
{
	className        : 'extjsf.Bind',
	extjsfBind       : true,

	init             : function()
	{
		//~: listeners by the names
		this._listeners = {}

		//=: nested binds (child components)
		this._items = []

		//~: properties
		this._props = { 'props': {}}

		//WARNING: this prevents recursion in Ext.clone()!
		this.constructor = null
	},

	/**
	 * Has three forms of a call:
	 *
	 * · returns the Ext JS component bound;
	 *
	 * · assigns the component if current
	 *   is not defined;
	 *
	 * · when true, installs the component
	 *   on the first demand.
	 *
	 * Always returns a component, or undefined.
	 */
	co               : function()
	{
		if(!arguments.length)
			return this._component
		ZeT.assert(arguments.length == 1)

		var co = arguments[0]

		if(!ZeT.isb(co)) //?: {a component-like}
			this._component = ZeT.assertn(co)
		else if(!this._component && (co === true))
			this._component = this.$create()

		return this._component
	},

	/**
	 * Invoked by a Domain when registering Bind.
	 */
	defined          : function(domain, name)
	{
		ZeT.assert(ZeT.iss(domain))
		ZeT.asserts(name)

		this.domain = domain
		this.name   = name
	},

	/**
	 * Tells JSF (facelet) component id and assigns
	 * the id and Bind name related attributes.
	 */
	ids              : function(clientId)
	{
		//~: JSF component id
		this._client_id = ZeT.asserts(clientId)

		//~: Ext JS component id
		this.props({ id: this.name })

		return this
	},

	viewId           : function(viewId)
	{
		this._view_id = ZeT.asserts(viewId)
		return this
	},

	/**
	 * This call (not required when render-to) tells
	 * the name of the Bind (co-ids):
	 *
	 * 0) of the parent JSF component this one
	 *    is directly placed in;
	 *
	 * 1) the component you want to place this one
	 *    instead of the default defined in [0].
	 *
	 * Second argument allows to implement extension
	 * points: you place component in node of else
	 * sub-tree of JSF components tree.
	 *
	 * Third optional argument (defaults to 'added')
	 * tells the target event name when to add.
	 * Works only in [1] case.
	 *
	 * When function is invoked wothout arguments,
	 * returns the effective parent Bind.
	 */
	parent           : function(parent_coid, target_coid, when)
	{
		if(!arguments.length)
		{
			if(this._target_coid)
				return extjsf.bind(this._target_coid, this.domain)
			else if(this._parent_coid)
				return extjsf.bind(this._parent_coid, this.domain)
			return undefined
		}

		//~: default parent
		delete this._parent_coid
		if(!ZeT.ises(parent_coid))
			this._parent_coid = parent_coid

		//~: targeted parent
		delete this._target_coid
		if(!ZeT.ises(target_coid))
			this._target_coid = target_coid

		//~: add-when event name
		delete this._add_when
		if(this._target_coid && !ZeT.ises(when))
			this._add_when = when

		return this
	},

	/**
	 * Tells DOM node ID to render this component to.
	 * Optional parent arguments tells the name of
	 * the Bind (of the same domain) to attach this
	 * component to. This allows to destroy this
	 * component with the target one.
	 *
	 * Note that when rendering components to the
	 * HTML content nested in Ext JS menus, parent
	 * argument is essential, or menu would be closed
	 * before click event over the component reaches it!
	 */
	renderTo         : function(nodeid, parent)
	{
		//~: render node id
		delete this._render_to
		if(!ZeT.ises(nodeid = ZeTS.trim(nodeid)))
			this._render_to = nodeid

		//?: {has render parent}
		delete this._render_parent
		if(this._render_to && !ZeT.ises(parent))
			this._render_parent = parent

		return this
	},

	/**
	 * Couples this Bind (and the future component)
	 * to the proper container or render place.
	 *
	 * Note that this call may be done before
	 * nested components (if any) are inserted
	 * as JSF facet and their Binds created!
	 */
	install          : function()
	{
		//?: {default add item}
		if(this.$is_install())
			this.$install()

		return this
	},

	/**
	 * Invoked always after the JSF facets with the
	 * nested components are rendered, and always
	 * after the install. Does all required the
	 * component to work.
	 *
	 * Note that at this point Ext JS components
	 * are still might be not available!
	 */
	ready            : function()
	{
		//?: {installed or added} do nothing
		if(this._installed_to || this._added_to)
			return this

		//?: {add to targeted parent}
		if(this._target_coid)
			this.$add_to()

		//?: {rendering to a node}
		else if(this._render_to)
			this.$render_to()

		return this
	},

	/**
	 * Invoked (mostly by a Domain) to destroy
	 * this Bind and the component it refers to.
	 */
	destroy          : function(opts)
	{
		//?: {skip this component}
		if(opts && ZeT.isa(opts.except))
			if(opts.except.indexOf(this) != -1)
				return

		return this.$destroy()
	},

	/**
	 * Returns destroy() call bound to this Bind.
	 * The same function is returned on every call.
	 */
	boundDestroy     : function()
	{
		if(!this._bound_destroy)
			this._bound_destroy =
			  ZeT.fbind(this.destroy, this)

		return this._bound_destroy
	},

	/**
	 * Invokes ZeT.scope() giving the first
	 * argument this Bind instance.
	 */
	scope            : function()
	{
		ZeT.assertf(arguments[arguments.length - 1])

		//~: argument this bind
		var a = ZeT.a(arguments)
		a.splice(0, 0, this)

		ZeT.scope.apply(ZeT, a)
		return this
	},

	/**
	 * Analogue of scope(), but is invoked only
	 * when the component bound is created (added
	 * to the parent container).
	 */
	when             : function()
	{
		if(this.co())
			this.scope.apply(this, arguments)
		else
			this.on('added', ZeT.fbinda(
			  this.scope, this, arguments, true))

		return this
	},

	/**
	 * Allows to define (and register in the same
	 * domain) a Bind based on this one. The name
	 * of the new Bind is: this bind name + '-' +
	 * nested bind name. Note this when inserting
	 * default facet with the properties!
	 *
	 * Optional second argument is a Bind instance,
	 * or a factory function, or ZeT Class.
	 *
	 * Callback function is invoked with arguments
	 * (this-context equals to this Bind, [1]):
	 *
	 *  0) new (nested) Bind instance;
	 *  1) this (nesting) Bind instance.
	 *
	 * If callback returns false, nested bind is
	 * unregistered from the domain.
	 */
	nest             : function(/* name, [ Bind, ] callback */)
	{
		//?: {this bind is not registered}
		ZeT.asserts(this.name)
		ZeT.assert(ZeT.iss(this.domain))

		var bind, callback = arguments[1]
		var name = ZeT.asserts(arguments[0],
		  'Nested Bind must have not an empty name!')

		if(arguments.length != 2)
		{
			ZeT.assert(arguments.length == 3)
			bind = callback; callback = arguments[2]
		}

		//?: {has no callback}
		ZeT.assertf(callback)

		//~: build the Bind instance
		if(ZeT.isx(bind))
			bind = new extjsf.Bind()
		else if(ZeT.isclass(bind))
			bind = bind.create()
		else if(ZeT.isf(bind))
			bind = bind()

		//?: {not a Bind}
		ZeT.assert(extjsf.isbind(bind))

		//~: initialize
		bind = this.$nest(name, bind)

		//!: invoke the callback
		if(bind && (false === callback.call(this, bind, this)))
			extjsf.domain(this.domain).unbind(bind)
		else
			this.$when_nested(name, bind)

		return this
	},

	/**
	 * Assigns the properties of the component.
	 */
	props            : function(/* [ suffix, ] props */)
	{
		var s = 'props', p = arguments[0]

		//?: {suffix is specified}
		if(arguments.length != 1)
		{
			ZeT.assert(arguments.length == 2)
			s = p; p = arguments[1]
		}

		ZeT.asserts(s)
		ZeT.assert(ZeT.iso(p))

		//~: accumulate the properties
		this._props[s] =
			ZeT.extend(this._props[s], p)

		return this
	},

	/**
	 * Same as props(), but takes only the properties
	 * that are not already defined for the component.
	 */
	merge            : function(/* [ suffix, ] props */)
	{
		var s = 'props', p = arguments[0]

		//?: {suffix is specified}
		if(arguments.length != 1)
		{
			ZeT.assert(arguments.length == 2)
			s = p; p = arguments[1]
		}

		ZeT.asserts(s)
		ZeT.assert(ZeT.iso(p))

		//~: accumulate the properties
		this._props[s] =
			ZeT.deepExtend(this._props[s], p)

		return this
	},

	/**
	 * Reads properties written from the configuration
	 * facet to the internal properties node. Optional
	 * suffix (default is 'props') allows to select DOM
	 * node (containing the properties text) from ones
	 * generated by ExtJSF components with facets.
	 *
	 * If optional compare argument is set to true,
	 * checks and returns whether any data were
	 * loaded from the node. (Optional suffix
	 * may be omitted in this call.)
	 */
	readPropsNode    : function(suffix, compare)
	{
		if(ZeT.isb(suffix))
			{ compare = suffix; suffix = null }

		//?: {default suffix}
		if(ZeT.ises(suffix)) suffix = 'props'

		//~: evaluate the properties node
		var props = this.$eval_props_node(suffix)

		//!: update the properties now
		this.props(suffix, props)

		return (compare)?(!!ZeT.keys(props).length):(this)
	},

	/**
	 * Private. Direct access to the properties.
	 */
	$raw             : function(suffix)
	{
		if(ZeT.ises(suffix)) suffix = 'props'
		return this._props[suffix]
	},

	/**
	 * Private. Creates the component.
	 */
	$create          : function()
	{
		return Ext.ComponentManager.create(this.buildProps())
	},

	$destroy         : function()
	{
		var co; if(co = this.co()) try
		{
			if(ZeT.isf(co.destroy))
				co.destroy()

			return true
		}
		finally
		{
			delete this._component
		}
	},

	$is_install      : function()
	{
		//?: {is not rendering to some else place}
		return !!this._parent_coid &&
		  !this._render_to && !this._target_coid
	},

	/**
	 * Private. Internals of install(). Add the properties
	 * of this Bind to be in the list of child items of
	 * the properties of the parent Bind. Works only when
	 * the parent component is not yet instantiated.
	 *
	 * This is the most common method of building a tree
	 * of components. It conflicts with render-to.
	 */
	$install         : function()
	{
		//?: {allowed to install}
		ZeT.assert(this._parent_coid && !this._added_to
		  && !this._installed_to && !this._rendered_to)

		//~: the domain
		var d = ZeT.assertn(extjsf.domain(this.domain),
		  'This Bind is not registered in any Domain!')

		//~: default parent bind
		var p = ZeT.assertn(d.bind(this._parent_coid),
		  'Parent Bind [', this._parent_coid, '] is not',
		  ' found in the Domain [', this.domain, ']!')

		p.addItem(this) //!: add this bind as a child
		this._installed_to = ZeT.ises(p.coid)?(p):(p.coid)
	},

	$add_to          : function()
	{
		//?: {allowed to add}
		ZeT.assert(this._target_coid && !this._added_to
		  && !this._installed_to && !this._rendered_to)

		//~: take the parent bind
		var p = ZeT.assertn(
		  extjsf.bind(this._target_coid, this.domain),
		  'Not found target Bind [', this._target_coid,
		  '] in domain [', this.domain, ']!')

		this._added_to = this._target_coid
		this.$do_add(p) //!: do add
	},

	$do_add           : function(p)
	{
		var self = this

		function doadd(x)
		{
			if(!x && (x.isComponent !== true)) x = p.co()
			ZeT.assert(x && (x.isComponent === true))
			x.add(self.co(true))
		}

		//?: {use defined event name}
		if(this._add_when)
			p.on(this._add_when, doadd)
		else if(p.co()) //?: {parent already exists}
			p.co().add(this.co(true))
		//~: delay the addition
		else p.on('added', doadd)
	},

	/**
	 * Private. Invoked when the component is rendered
	 * into a DOM node, but not installed into the parent
	 * container as a regular child. Allows to control
	 * the destruction of the component and helps with
	 * tricky handling of click events.
	 */
	$render_to       : function()
	{
		//?: {allowed to install}
		ZeT.assert(this._render_to && !this._added_to
		  && !this._installed_to && !this._rendered_to)

		this._rendered_to = true

		//?: {no bound parent}
		if(!this._render_parent)
			return this.$do_render(this._parent_coid &&
			  extjsf.bind(this._parent_coid, this.domain))

		var p = ZeT.assertn(
		  extjsf.bind(this._render_parent, this.domain),
		  'Bind of the render parent [', this._render_parent,
		  '] is not found at domain [', this.domain, ']!'
		)

		//~: bind the destroy
		p.on('beforedestroy', this.boundDestroy())

		//~: access the parent bind
		this.$do_render(p, this.$render_parent)
	},

	$do_render       : function(p, callback)
	{
		if(!extjsf.isbind(p)) return

		function doit(pco)
		{
			//~: create the component
			this.co(true)

			//~: invoke the callback
			if(callback) callback.call(this, pco)
		}

		//?: {parent is rendered, node exists}
		if(p.co() && (p.co().rendered || Ext.getDom(this._render_to)))
			return doit.call(this, p.co())

		p.on('render', ZeT.fbind(doit, this))
	},

	$render_parent   : function(p)
	{
		var co = ZeT.assertn(this.co())

		//--> up()
		//  used when component is rendered in menu,
		//  or client event will close the menu before
		//  clicking the component!
		var up = co.up; co.up = function(selector)
		{
			//~: invoke original up()
			var r = up.apply(co, arguments)
			if(r) return r

			//?: {is that component}
			if(p === selector)
				return p
			else if(ZeT.iss(selector) && p.is(selector))
				return p

			//~: invoke the render-parent
			return p.up.apply(p, arguments)
		}
	},

	/**
	 * Private. Returns ID of DOM node that
	 * stores the properties of this component.
	 */
	$node_id         : function(suffix)
	{
		var nid

		if(!ZeT.ises(this._node_id))
			nid = this._node_id

		//?: {form from the client id}
		if(!ZeT.ises(this._client_id))
			nid = ZeTS.cat(this._client_id, '-',
			  ZeT.asserts(this.name))

		if(ZeT.iss(suffix) && nid)
			nid = ZeTS.cat(nid, '-', suffix)

		return nid
	},

	/**
	 * Private. Searches for the properties node with
	 * the suffix given and evaluates the properties.
	 */
	$eval_props_node : function(suffix)
	{
		//~: text content of the node
		var x = ZeTX.text(this.$props_node(suffix))

		//~: evaluate the result
		return ZeTS.ises(x)?{}:this.$eval_props(x)
	},

	/**
	 * Private. Searches for the properties node with
	 * the suffix given. Returns Ext.Element.
	 */
	$props_node      : function(suffix)
	{
		return Ext.getDom(ZeTS.cat(this.$node_id(), '-', ZeT.asserts(suffix)))
	},

	/**
	 * Private. Evaluates the properties written
	 * via a facet as JS-object (except the curly
	 * brackets) into real object.
	 */
	$eval_props      : function(props)
	{
		if(ZeT.ises(props)) return
		props = ZeTS.trim(props)

		if(!ZeTS.starts(props, '('))
		{
			if(!ZeTS.starts(props, '{', '['))
				props = ZeTS.cat('{', props, '}')

			props = ZeTS.cat('(', props, ')')
		}

		try
		{
			return eval(props)
		}
		catch(e)
		{
			throw ZeT.ass('Illegal properties of Bind [',
			  this.name, ']: \n', props, '\n', e)
		}
	},

	/**
	 * Private. Initializes the nested Bind.
	 */
	$nest            : function(name, bind)
	{
		//~: register the nested bind
		extjsf.domain(this.domain).
		  bind(ZeTS.cat(this.name, '-', name), bind)

		//~: same client id, component id
		bind.ids(this._client_id)

		//~: parent bind
		bind.parent(this._parent_coid, this._target_coid)

		return bind
	},

	$when_nested     : function(name, bind)
	{
		this.on('beforedestroy', bind.boundDestroy())
	},

	/**
	 * Private. Adds request parameters to call
	 * this component on the server side.
	 */
	$add_params      : function(params)
	{
		//~: default ExtJSF domain
		if(!ZeT.iss(params.domain) && !ZeT.ises(this.domain))
			params.domain = this.domain

		//~: default view id
		if(!ZeT.iss(params.view) && !ZeT.ises(this._view_id))
			params.view = this._view_id

		return params
	}
})


// +----: Action Bind :------------------------------------------+

extjsf.ActionBind = ZeT.defineClass(
  'extjsf.ActionBind', extjsf.Bind,
{
	className        : 'extjsf.ActionBind',

	init             : function()
	{
		this.$applySuper(arguments)

		//=: bound handler
		this.handler = ZeT.fbind(this.$handler, this)
	},

	postMode         : function(mode)
	{
		this._post_mode = ZeT.asserts(mode)
		return this
	},

	/**
	 * Takes HTML form related to this Bind and updates
	 * the action attribute to make it a go-link.
	 *
	 * Go links look like '/context/go/.. component ..'
	 * where the path doesn't end with '.xhtml' suffix.
	 *
	 * Using this call assumes that Go-dispatching
	 * filter is configured on the server side!
	 */
	goAction         : function()
	{
		//~: action attribute
		var a = this.$form_action(), p = '', i

		//?: {action already has '/go/'}
		if(a.indexOf('/go/') != -1)
			return this

		//~: action prefix
		if(ZeTS.starts(a, '/'))
			{ p = '/'; a = a.substring(1) }

		if((i = a.indexOf('/')) != 0)
		{
			p += a.substring(0, i + 1)
			a  = a.substring(i + 1)
		}

		if(!ZeTS.ends(p, '/')) p += '/'

		//~: strip the suffix
		if(ZeTS.ends(a, '.xhtml'))
			a = a.substring(0, a.length - 6);

		//!: update the action attribute
		this.$form_node().set({ action: p + 'go/' + a })

		return this
	},

	/**
	 * Call strategy of the action. Posts the form.
	 */
	$handler         : function(opts)
	{
		var self = this

		//~: copy the options
		opts = ZeT.extend({}, opts)

		//~: request address
		opts.url = this.$form_action()

		//~: parameters of the request
		opts.params = this.$post_params(opts.params, true, true)

		//~: success callback
		var onsuccess = opts.success
		opts.success = function(response)
		{
			if(extjsf.responseHandler.call(self, self.domain, response))
				ZeT.isf(onsuccess) && onsuccess.apply(self, arguments)
			else
				ZeT.isf(opts.failure) && opts.failure.apply(self, arguments)
		}

		//!: issue the request
		Ext.Ajax.request(opts)
	},

	/**
	 * Private. Returns Ext.Element of the form node
	 * found by id based on the $node_id().
	 */
	$form_node       : function(optional)
	{
		//~: take the basic node
		var n = this.$node_id()
		if(ZeT.ises(n))
			if(optional === true) return; else
				throw ZeT.ass('Bind form id is not provided!')

		//~: take the node
		var e = Ext.get(n)
		if(!e) if(optional === true) return; else
			throw ZeT.ass('Bind form DOM node [', n , '] is not found!')

		//?: {not a form}
		var tag = ZeT.asserts(ZeT.get(e, 'dom', 'tagName'))
		ZeT.assert(tag.toLowerCase() == 'form',
		  'DOM node by id [', n, '] is not a form!')

		return e
	},

	$form_action     : function()
	{
		var f = this.$form_node()
		return ZeT.asserts(f.getAttribute('action'),
		  'ExtJSF form [', f.id, '] has no action attribute!')
	},

	/**
	 * Private. Collects parameters from the form. Optional
	 * argument allows to specify form action button to include
	 * into the result. (To invoke the action.)
	 */
	$form_params     : function(button, except)
	{
		var res = {}

		//~: select the form inputs
		this.$form_node().select('input').each(function(item)
		{
			var n = item.getAttribute('name'); if(!n) return
			var v = item.getAttribute('value') || '';

			//?: {this field is a submit button} skip it
			if(item.getAttribute('type') == 'submit')
				if((button !== true) && (button !== n))
					return

			if(!except || !except[n])
				res[n] = v
		})

		return res
	},

	/**
	 * Private. Collects parameters for POST request.
	 */
	$post_params     : function(added, form, button)
	{
		//~: request parameters from the facet
		var params = ZeT.extend({}, this.$raw('params'))

		//~: collect parameters from the form
		if(form !== false)
			ZeT.extend(params, this.$form_params(button))

		//~: parameters from the options
		ZeT.extend(params, added)

		//~: resolve delayed parameters
		ZeT.undelay(params)

		//~: additional parameters
		return this.$add_params(params)
	},

	$add_params      : function(params)
	{
		params = this.$applySuper(arguments)

		//~: default view mode
		if(!ZeT.iss(params.mode) && !ZeT.ises(this._post_mode))
			params.mode = this._post_mode

		return params
	}
})


// +----: Form Bind :-------------------------------------------+

extjsf.FormBind = ZeT.defineClass(
  'extjsf.FormBind', extjsf.ActionBind,
{
	className        : 'extjsf.FormBind',

	/**
	 * Assigns additional validator of the form
	 * that is invoked as v.call(bind, form, opts).
	 * Returns this bind.
	 *
	 * Note that this validator does supress
	 * the built-in of the form component.
	 */
	validator        : function(v)
	{
		delete this._validator
		if(ZeT.isf(v)) this._validator = v
		return this
	},

	/**
	 * Binds form's submit() with the options given.
	 * If no options given, returns the previously
	 * bound function (if any), or creates a new one.
	 */
	submitBound      : function(opts)
	{
		if(arguments.length)
		{
			ZeT.assert(arguments.length == 1)
			ZeT.assert(ZeT.iso(arguments[0]))

			this._bound_submit = ZeT.fbind(
			  this.submit, this, arguments[0])
		}
		else if(!this._bound_submit)
			this._bound_submit = ZeT.fbind(this.submit, this)

		return this._bound_submit
	},

	/**
	 * Submits the form by sending POST request
	 * to the back bean (JSF view) configured.
	 * Does validation before sending.
	 *
	 * The options may contain these fields:
	 *
	 *  · success   alternative callback on form
	 *    submit request. See callback();
	 *
	 *  · failure   alternative callback on form
	 *    submit failure. See failure();
	 *
	 *  · params    additional parameters of the
	 *    form post request.
	 *
	 * Returns false when form is invalid, and
	 * no post request was issued.
	 *
	 * Action is Ext.form.action.Action.
	 */
	submit           : function(opts)
	{
		opts = ZeT.extend({}, opts)

		//~: get the Ext form component
		var fco = this.$form_co()

		//~: {the form is not valid}
		if(!this.$validate(opts))
			return false

		//~: submit parameters
		var ps  = this.$post_params(opts)

		//~: action url
		var url = this.$form_action()

		//!: do submit Ext JS form
		fco.submit({ url: url, params: ps,
		  success: ZeT.fbind(this.$eval_result, this, opts),
		  failure: ZeT.fbind(this.$call_failure, this, opts),
		  clientValidation: false //<-- our validation instead
		})

		return this
	},

	/**
	 * Register the callback on successfull form submit.
	 * Callback is invoked as:
	 *
	 *   cb.call(bind, submit opts, data, action, form)
	 *
	 * Data is object returned from the server (XML
	 * document for body-post response format.)
	 */
	success          : function(cb)
	{
		if(!ZeT.isf(cb))
			return this._on_success
		this._on_success = cb
		return this
	},

	/**
	 * Register the callback on failed form submit,
	 * of the validation failure.
	 */
	failure          : function(cb)
	{
		if(!ZeT.isf(cb))
			return this._on_failure
		this._on_failure = cb
		return this
	},

	$form_co         : function()
	{
		return ZeT.assert(this.co() &&
		  ZeT.isf(this.co().getForm) && this.co().getForm(),
		  'Can not process undefined form [', this.name,
		  '] in domain [', this.domain, ']'
		)
	},

	$post_params     : function(opts)
	{
		//~: form action command (JSF input id and name)
		var button = ZeT.get(opts, 'command')
		if(!ZeT.ises(button)) button = this.$node_id(button)

		//~: additional parameters from the options
		var params = ZeT.get(opts, 'params')

		//~: collect all parameters of the action
		return this.$callSuper(params, true, button)
	},

	$form_params     : function(button, except)
	{
		//~: add ext fields to exception
		except = ZeT.extend(this.$ext_fields(), except)

		return this.$callSuper(button, except)
	},

	/**
	 * Private. Maps fields present in Ext JS form
	 * component by their request (post) names.
	 */
	$ext_fields      : function()
	{
		var res = {}, fco = this.$form_co()

		fco.getFields().each(function(f) {
			res[f.getName()] = f
		})

		return res
	},

	$validate        : function(opts)
	{
		var fco = this.$form_co()

		//?: {default is not valid}
		if(!this._validator && !fco.isValid())
			return false

		//?: {external validator is not}
		else if(this._validator)
			if(false === this._validator.call(this, fco, opts))
				return false

		return true
	},

	$eval_result     : function(opts, form, action)
	{
		var self = this, success = true, xml

		//?: {action has no result}
		ZeT.assertn(action.result, 'Submit of form [', this.name,
		  '] returned no action result! Check:', action)

		//~: display errors of the fields
		ZeT.each(action.result.errors, function(er)
		{
			if(ZeTS.ises(er.target)) return

			//~: get the bind in this domain
			var bn = extjsf.bind(er.target, self.domain)
			var co = bn && bn.co()

			//?: {not found, try else}
			if(!co) co = Ext.getCmp(er.target)

			//?: {component is field}
			if(co && (co.isFormField === true))
				co.markInvalid(er.error)

			success = false
		})

		//~: success status in the response attribute
		if(xml = ZeT.get(form, 'errorReader', 'rawData')) try
		{
			var root   = ZeTX.node(xml, 'validation')
			var status = root && ZeTX.attr(root, 'success')
			if(status) success = ('true' === status)
		}
		catch(e)
		{
			success = false
		}

		if(success)
			this.$call_success(opts, xml, action, form)
		else
			this.$call_failure(opts, xml, action, form)
	},

	$call_success    : function(opts, xml)
	{
		//~: evaluate the scripts
		this.$call_scripts(xml)

		if(ZeT.isf(this._on_success))
			this._on_success.apply(this, arguments)

		if(ZeT.isf(opts.success))
			opts.success.apply(this, arguments)
	},

	$call_failure    : function(opts, xml)
	{
		//~: evaluate the scripts
		this.$call_scripts(xml)

		if(ZeT.isf(this._on_failure))
			this._on_failure.apply(this, arguments)

		if(ZeT.isf(opts.failure))
			opts.failure.apply(this, arguments)
	},

	/**
	 * Private. Takes all script tags in the XML
	 * document and evaluates them.
	 */
	$call_scripts    : function(xml)
	{
		var scripts = ZeTX.nodes(xml, 'script')
		if(!scripts || !scripts.length) return

		var self = this
		ZeT.each(scripts, function(script)
		{
			try
			{
				ZeT.xeval(script)
			}
			catch(e)
			{
				ZeT.log('Error evaluating script after form [',
				  self.name, '] submit: \n', script, '\n',  e)

				throw e
			}
		})
	}
})

/**
 * Form validation response model.
 */
Ext.define('extjsf.model.FormValidation',
{
	extend: 'Ext.data.Model',

	idProperty: 'target',

	fields: [

		{name: 'target', type: 'string', mapping: '@target'},
		{name: 'error',  type: 'string'}
	]
})


// +----: Field Bind :------------------------------------------+

extjsf.FieldBind = ZeT.defineClass(
  'extjsf.FieldBind', extjsf.Bind,
{
	className        : 'extjsf.FieldBind',

	ids              : function()
	{
		this.$applySuper(arguments)

		//~: JSF field related properties
		this.props({ inputId: this.$node_id(),
		  name: this.$field_name() })

		return this
	},

	buildProps       : function()
	{
		var res = this.$applySuper(arguments)

		//~: field value
		if(ZeT.isu(res.value))
		{
			var f = Ext.get(this.$field_name())
			if(f) res.value = f.getValue()
		}

		return res
	},

	/**
	 * Creates nested Bind of the label component
	 * ('label' suffix) to add it before or after
	 * the field component during the install.
	 */
	buildLabel       : function(always)
	{
		return this.nest('label',
		  ZeT.fbindu(this.$build_label, this, 1, always))
	},

	install          : function()
	{
		if(!this.label) //?: {has no label}
			return this.$applySuper(arguments)

		//?: {label is before}
		var a = this.label.$raw().labelAlign
		var x = (a === 'right') || (a === 'after')
		if(!x) this.label.install()

		//!: install this field
		this.$applySuper(arguments)

		//~: install label after
		if(x) this.label.install()

		return this
	},

	$build_label     : function(label, always)
	{
		//~: take properties from the field bind
		label.props(this.$raw('label') || {})

		//~: label defaults
		label.props({ xtype: 'label',
		  forId: this.$node_id() })

		//?: {empty label}
		if(!label.readPropsNode(true) && !always)
			return false

		//~: save label to install
		this.label = label

		//~: hide the inlined label
		this.props({ hideLabel: true })

		//~: add the event listeners
		label.on('afterrender',
		  ZeT.fbind(this.$label_events, this))
	},

	/**
	 * Private. Installs events on the label node.
	 * (Invoked after the label is rendered.)
	 */
	$label_events    : function()
	{
		var self = this

		//~: click on the label
		this.label.co().getEl().addListener('click',
		  ZeT.fbind(this.$label_click, this))

		//~: label cursor
		var cursor; function setCursor()
		{
			var el = self.label.co().getEl()

			if(self.co().readOnly)
			{
				if(!cursor) cursor = el.getStyle('cursor')
				el.setStyle('cursor', 'default')
			}
			else if(cursor)
				el.setStyle('cursor', cursor)
		}

		//~: set cursor now
		setCursor()

		//~: mouse entered (set cursor)
		this.label.co().getEl().
		  addListener('mouseenter', setCursor)
	},

	$label_click     : function(e)
	{
		var el = this.label.co().getEl()

		if(this.co().readOnly)
			e.stopEvent()
		else
			this.$field_focus()
	},

	$field_focus     : function()
	{
		//?: {field has picker} expand it
		if(ZeT.isf(this.co().getPicker))
			if(this.co().getPicker())
				return this.co().expand()

		this.co().focus()
	},

	$field_name      : function()
	{
		return this.$node_id('field')
	}
})


// +----: Drop Field Bind :-------------------------------------+

extjsf.DropFieldBind = ZeT.defineClass(
  'extjsf.DropFieldBind', extjsf.FieldBind,
{
	className        : 'extjsf.DropFieldBind',

	$install         : function()
	{
		//~: refresh on double-expand
		this.$expand_refresh()

		//~: label inline store
		if(ZeT.isx(this.$raw().store))
			this.$labels_store()

		//~: set display value
		this.$display_value()

		this.$applySuper(arguments)
	},

	$labels_store    : function()
	{
		var labels = this.$eval_props_node('labels')
		if(!ZeT.isa(labels)) return

		//~: convert 2-array to pairs
		for(var ls = [], i = 0;(i+1 < labels.length);i += 2)
			ls.push([ labels[i], labels[i+1] ])

		//!: set the inline store
		this.props({ store: ls, queryMode: 'local' })
	},

	/**
	 * Private. Directly assigns initial display
	 * value to the input node of the drop list.
	 */
	$display_value   : function()
	{
		var dv = this.$raw().displayValue
		delete this.$raw().displayValue

		if(!ZeT.isu(dv)) this.on('afterrender', function(f)
		{
			f.inputEl.set({ value: dv })
		})
	},

	$refresh_delay   : 2000,

	$expand_refresh  : function()
	{
		var nrone = this.$raw().notRefreshOnExpand
		delete this.$raw().notRefreshOnExpand
		if(nrone) return

		var et; this.on('expand', function()
		{
			var ts = new Date().getTime()
			if(et && (ts - et < this.$refresh_delay))
				bind.co().getStore().reload()
			et = ts
		})
	}
})


// +----: Checkboxes Bind :-------------------------------------+

extjsf.CheckboxesBind = ZeT.defineClass(
  'extjsf.CheckboxesBind', extjsf.Bind,
{
	className        : 'extjsf.CheckboxesBind',

	install          : function()
	{
		var self    = this, i = 0
		var checked = this.$read_checked()
		var labels  = this.$read_labels()

		//c: nest each checkbox
		labels.each(function(label, key)
		{
			self.$nest_check(i++, key, label,
			  checked.contains(key))
		})
	},

	$nest_check      : function(i, key, label, checked)
	{
		var self = this, bind = this.$check_bind.create()
		this.nest(''+i, bind, function()
		{
			self.$init_check(bind, i, key, label, checked)

			if(!self.checks) self.checks = []
			self.checks.push(bind)
		})
	},

	$check_bind      : extjsf.FieldBind,

	$init_check      : function(check, i, key, label, checked)
	{
		//~: clone the properties
		check.merge(ZeT.deepClone(this.$raw()))
		check.props('label', ZeT.deepClone(this.$raw('label')))

		//~: check box label
		check.props({ inputValue: key, checked: checked })
		delete check.$raw().value //<-- must have it not!

		//~: label text
		check.props('label', { text: label })

		//~: check handler
		check.on('change',
		  ZeT.fbind(this.$check_changed, this, check))

		//~: install the check box
		check.buildLabel(true).install()
	},

	$read_checked    : function()
	{
		var f = ZeT.assertn(Ext.get(this.$node_id()))
		var r = new ZeT.Map(), v = f.getValue()
		if(ZeT.ises(v)) return r

		ZeT.each(v.split(','), function(s)
		{
			if(!ZeT.ises(s = ZeTS.trim(s))) r.put(s)
		})

		return r
	},

	$read_labels     : function()
	{
		var  r = new ZeT.Map()
		var ls = this.$eval_props_node('labels')
		if(!ZeT.isa(ls)) return r

		//~: insert 2-array into the map
		for(var i = 0;(i+1 < ls.length);i += 2)
			r.put(ls[i], ls[i+1])

		return r
	},

	$check_changed   : function(check)
	{
		if(this._checking) return
		this._checking = check

		var x = check.$raw().inputValue
		var f = Ext.get(this.$node_id())
		var v = ZeT.ises(f.getValue())?[]:
		  f.getValue().split(',')

		//~: remove or add the value
		if(!check.value()) ZeTA.remove(v, x)
		else if(v.indexOf(x) < 0) v.push(x)

		//~: assign to the hidden field
		v = this.$set_checked(check, v)
		if(ZeT.isu(v)) v = [x]
		f.set({ value: v.join(',') })

		delete this._checking
	},

	$set_checked     : function(check, v)
	{
		//?: {unchecked all}
		if(!v.length && (check.$raw().allowNone === false))
			check.co().setValue(true)
		//?: {checked two of a radio}
		else if((v.length > 1) && check.$raw().onlyOne)
			ZeT.each(this.checks, function(c)
			{
				if((c != check) && c.value()) c.value(false)
			})
		else
			return v
	}
})


// +----: Load Action Bind :------------------------------------+

extjsf.LoadActionBind = ZeT.defineClass(
  'extjsf.LoadActionBind', extjsf.ActionBind,
{
	className        : 'extjsf.LoadActionBind',

	$handler         : function(opts)
	{
		//~: collect the parameters (omitting the form)
		var params = this.$post_params(
		  ZeT.get(opts, 'params'), false)

		//~: GET or POST
		var method = 'POST'; if(params.method === 'GET')
			{ method = 'GET'; delete params.method }

		//!: load the window
		new extjsf.WinmainLoader(this.domain).
		  form(this.$form_node()).button(true).
		  addParams(params).setMethod(method).
		  load()
	}
})


// +----: Store Bind :------------------------------------------+

extjsf.StoreBind = ZeT.defineClass(
  'extjsf.StoreBind', extjsf.Bind,
{
	className        : 'extjsf.StoreBind',

	install          : function()
	{
		this.$install()

		//~: create the component
		var self = this; Ext.onReady(function()
		{
			self.co(true) //~: create the component

			//~: set the proxy
			if(self.proxy)
				self.proxy.co(self.co().getProxy())
		})

		return this
	},

	$install         : function()
	{
		//?: {has no page size}
		var ps = this.$raw().pageSize
		ZeT.assert(ZeT.isu(ps) || (ZeT.isi(ps) && (ps >= 0)))
		if(ps === 0) delete this.$raw().pageSize

		if(this.proxy) //?: {has proxy configured}
			this.$install_proxy()

		//~: sort the store before each load
		this.on('beforeload', ZeT.fbind(this.$before_load, this))
	},

	$create          : function()
	{
		return Ext.create('Ext.data.Store', this.buildProps())
	},

	$destroy         : function()
	{
		var co = this.co()
		var de = this.$applySuper(arguments)

		//?: {component is destroyed}
		if(co && (de === true))
			//?: {store is still registered}
			if(co == Ext.data.StoreManager.lookup(this.name))
				Ext.data.StoreManager.unregister(this.name)

		return de
	},

	$install_proxy   : function()
	{
		var proxy = ZeT.assertn(this.proxy)

		//~: proxy parameters
		this.$proxy_params(proxy)

		//!: add proxy properties to the store
		this.props({ proxy: proxy.$raw() })
	},

	$proxy_params    : function(proxy)
	{
		//~: proxy request parameters
		var extra  = ZeT.assertn(proxy.$raw('extra'))
		var params = proxy.$raw().extraParams
		if(!params) proxy.props({ extraParams: (params = {})})
		ZeT.assert(ZeT.iso(params))

		//~: model key parameter
		if(ZeT.ises(params.model))
			//?: {direct model key}
			if(!ZeT.ises(extra.key))
				params.model = extra.key
			//?: {model key from the view}
			else if(!ZeT.ises(extra.viewKey))
				params.model = extra.viewKey

		//~: model provider parameter
		if(ZeT.ises(params['model-provider']))
			if(!ZeT.ises(extra.provider))
				params['model-provider'] = extra.provider

		//~: model request parameter
		if(ZeT.ises(params['model-request']))
			if(!ZeT.ises(extra.request))
				params['model-request'] = extra.request

		return this.$add_params(params)
	},

	$before_load     : function()
	{
		//?: {has no proxy} forbid load
		if(!this.proxy) return false

		this.$update_params()
	},

	$update_params   : function()
	{
		var sp = {}, sos = this.co().sorters

		//~: collect sorters
		if(sos) for(var i = 0;(i < sos.getCount());i++)
		{
			//~: check sort property
			var so = sos.getAt(i)
			if(ZeT.ises(so._property)) { sp = {}; break }

			//[i]: sort property
			sp['sortProperty' + i] = so._property

			//[i]: is descending
			if('DESC' === so._direction)
				sp['sortDesc' + i] = true
		}

		//~: proxy parameters
		var params = this.proxy.co().getExtraParams()

		//~: remove previous sort parameters
		for(i = 0;(params['sortProperty' + i]);i++)
		{
			delete params['sortProperty' + i]
			delete params['sortDesc' + i]
		}

		//~: store bind name
		sp.bind = this.name

		//~: add new parameters
		ZeT.extend(params, sp)
	}
})


// +----: Components to Refactor :------------------------------->

ZeT.extend(extjsf,
{
	//=    Components Binding    =//

	/**
	 * Tries to get the Ext JS component from
	 * the arguments given.
	 *
	 * If arg[0] is a string, it defines the
	 * bind name, and optional arg[1] defines
	 * the domain. (Defaults to ''.)
	 *
	 * Second variant, arg[0] is a bind.
	 *
	 * Third variant, arg[0] is an options
	 * object with 'name' and optional 'domain',
	 * or 'bind' fields.
	 *
	 * Last, arg[0] may be a Component instance,
	 * or has 'component' field.
	 */
	co               : function()
	{
		var bind, arg = arguments[0]
		if(!arg) return undefined

		if(arg.isComponent === true) return arg
		if(arg.component && arg.component.isComponent === true)
			return arg.component

		if(ZeT.iss(arg))
		{
			bind = extjsf.bind(arg, arguments[1])
			return bind && bind.co()
		}

		if(arg.extjsfBind) return arg.co()

		if(ZeT.iss(arg.name))
		{
			bind = extjsf.bind(arg.name, arg.domain)
			return bind && bind.co()
		}

		if(arg.bind && arg.bind.extjsfBind)
			return arg.bind.co()

		return undefined
	},

	isbind           : function(bind)
	{
		return !!bind && (bind.extjsfBind === true)
	},

	asbind           : function(borc)
	{
		if(!borc) return undefined

		//?: {is a string}
		if(ZeT.iss(borc))
		{
			var domain = arguments[1]
			ZeT.assert(ZeT.iss(domain))
			return extjsf.bind(borc, domain)
		}

		//?: {is a component}
		if(borc.isComponent && borc.extjsfBind)
			borc = borc.extjsfBind

		//?: {is a bind}
		if(borc.extjsfBind === true)
			return borc

		return undefined
	},

	/**
	 * Here the bind is not defined, but added as
	 * an item to the bind defined by name (and
	 * domain) pair. The bind argument may be
	 * the bind string key registered in the same
	 * domain.
	 */
	bindAddItem      : function()
	{
		var name = arguments[0];
		var domn = arguments[1];
		var addb = arguments[2];

		if(!ZeT.iss(domn)) domn = '';

		if(!addb)
		{
			addb = arguments[1];
			domn = '';
		}

		var bind = this.bind(name, domn);
		if(!bind || !ZeT.isf(bind.addItem))
			return this;

		if(ZeT.iss(addb))
			addb = extjsf.bind(addb, domn);

		if(addb) bind.addItem(addb)
		return this;
	},

	tempDomain       : function(prefix)
	{
		if(!ZeT.iss(prefix)) prefix = ''
		prefix = 'tmp:' + prefix

		if(!this._temp_domain_id)
			this._temp_domain_id = 0
		prefix += this._temp_domain_id
		this._temp_domain_id++
		return prefix
	},

	globalDomain     : function(suffix)
	{
		return ZeTS.ises(suffix)?('Global'):('Global:' + suffix)
	},

	genViewId        : function()
	{
		if(ZeT.isu(extjsf._gen_view_id))
			extjsf._gen_view_id = 0;
		return 'extjsf_view_' + (new Date().getTime()) + '_' +
		  extjsf._gen_view_id++;
	},

	/**
	 * Assigns handler function of a Bind and
	 * the component bound. The arguments one
	 * of two variants:
	 *
	 *  · name, domain, [ handler ];
	 *
	 *  · bind or component, [ handler ].
	 *
	 * If handler function is not given, returns
	 * the function assigned to bind-component.
	 */
	handler          : function()
	{
		//~: search for the bind
		var b = extjsf.asbind.apply(ZeT, arguments)
		if(!b) return undefined

		//~: handler
		var h = ZeT.isf(arguments[2])?(arguments[2]):
		  ZeT.isf(arguments[1])?(arguments[1]):(null)

		//?: {assign the handler}
		if(b && ZeT.isf(h))
		{
			b.handler = ZeT.fbind(h, b)

			if(b.co() && ZeT.isf(b.co().setHandler))
				b.co().setHandler(b.handler)

			return this
		}

		if(ZeT.isf(b.handler))
			return b.handler

		//?: {handler in the component}
		if(b.co() && ZeT.isf(b.co().handler))
			return b.handler = b.co().handler
	},

	/**
	 * Same as handler(), but returns Ext.emptyFn
	 * function if component has it not assigned.
	 */
	xhandler         : function()
	{
		return extjsf.handler.
		  apply(extjsf, arguments) || Ext.emptyFn
	},

	handlerCaller    : function(name, domain)
	{
		return function()
		{
			var f = extjsf.handler(name, domain);
			if(ZeT.isf(f)) f.apply(this, arguments)
		}
	},

	delayCreate      : function(extClass, opts)
	{
		Ext.require(extClass)

		return ZeT.delay(function()
		{
			return Ext.create(extClass, opts);
		});
	},

	catchError       : function(e, that, args)
	{
		ZeT.log('Caught unhandled exception: ', e, that, args)
	},

	//=     Ajax Processing      =//

	/**
	 * Handles the results of POST calls resulting the
	 * 'http://extjs.jsf.java.net/response' XMLs.
	 *
	 * Returns true when the validation is correct.
	 */
	responseHandler   : function(domain, response)
	{
		var xml = response && response.responseXML;
		if(!xml) throw 'AJAX Response Handler got no result XML!';

		//~: process validated fields
		var val = ZeTX.node(xml, 'validation');
		var scr, fds = val && ZeTX.nodes(val, 'field');
		if(fds) for(var i = 0;(i < fds.length);i++)
		{
			var target = ZeTX.attr(fds[i], 'target');
			var field  = extjsf.co(target, domain);

			if(field && field.isFormField)
			{
				var error = ZeTS.trim(ZeTX.text(ZeTX.node(fds[i], 'error')))
				if(error.length) field.markInvalid(error)
			}

			scr = ZeTX.node(fds[i], 'script');
			if(scr) try
			{
				ZeT.xeval(ZeTX.text(scr))
			}
			catch(e)
			{
				ZeT.log('error in validation script for field [',
				  target, ']: \n', ZeTX.text(scr)
				)
				throw e;
			}
		}

		//~: validation script
		scr = val && ZeTX.node(val, 'script');
		if(scr) try
		{
			ZeT.xeval(ZeTX.text(scr))
		}
		catch(e)
		{
			ZeT.log('error in validation block script: \n', ZeTX.text(scr))
			throw e;
		}

		//~: process additional scripts
		var scrs = ZeTX.nodes(ZeTX.node(xml, 'scripts'), 'script');
		if(scrs) for(var si = 0;(si < scrs.length);si++) try
		{
			ZeT.xeval(ZeTX.text(scrs[si]))
		}
		catch(e)
		{
			ZeT.log('error in response script: \n', ZeTX.text(scrs[si]))
			throw e;
		}

		return !val || (ZeTX.attr(val, 'success') == 'true');
	},


	//=       CSS  Support       =//

	/**
	 * Returns the integer number of pixels in
	 * the given (float) number of CSS ex-size.
	 *
	 * Argument 'vpx' if defined just adds the
	 * given number of plain pixels to the result.
	 */
	ex               : function(v, vpx)
	{
		return this._css_sz(10, 'ex', v) + (ZeT.isn(vpx)?(vpx):(0));
	},

	dex              : function()
	{
		return ZeT.delay(ZeT.fbinda(this.ex, this, arguments));
	},

	exs              : function(ispx)
	{
		var s = '', a = arguments;
		var x = (ispx === true);

		if(ZeT.isb(ispx))
			(a = ZeT.a(a)).shift();

		for(var i = 0;(i < a.length);i++)
		{
			if(s.length) s += ' ';
			s += this.ex(a[i]);
			if(x) s += 'px';
		}
		return s;
	},

	dexs             : function()
	{
		return ZeT.delay(ZeT.fbinda(this.exs, this, arguments));
	},

	/**
	 * Returns the integer number of pixels in
	 * the given (float) number of CSS pt-size.
	 *
	 * Argument 'vpx' if defined just adds the
	 * given number of plain pixels to the result.
	 */
	pt               : function(v, vpx)
	{
		return this._css_sz(100, 'pt', v) + (ZeT.isn(vpx)?(vpx):(0));
	},

	dpt              : function()
	{
		return ZeT.delay(ZeT.fbinda(this.pt, this, arguments));
	},

	pts              : function(ispx)
	{
		var s = '', a = arguments;
		var x = (ispx === true);

		if(ZeT.isb(ispx))
			(a = ZeT.a(a)).shift();

		for(var i = 0;(i < a.length);i++)
		{
			if(s.length) s += ' ';
			s += this.pt(a[i]);
			if(x) s += 'px';
		}
		return s;
	},

	dpts             : function()
	{
		return ZeT.delay(ZeT.fbinda(this.pts, this, arguments));
	},

	inch             : function(v, vpx)
	{
		return this._css_sz(2, 'in', v) + (ZeT.isn(vpx)?(vpx):(0));
	},

	_css_sz          : function(width, units, v)
	{
		var key = '__css_sz_' + width + 'units';
		var wds = this[key];

		if(wds) return Math.round(wds * v / width);

		var div = document.createElement('div');
		div.style.visibility = 'hidden';
		div.style.width  = '' + width + units;
		div.style.height = '1px';

		document.body.appendChild(div)
		wds = 1.0; this[key] = wds = wds * div.offsetWidth;

		if(wds == 0.0) throw 'Can not define ' +
		  'DOM element offset width at this call time!';

		return Math.round(wds * v / width);
	},

	_css_szs         : function(css_sz, args, ispx)
	{
		var s = ''; for(var i = 0;(i < args.length);i++)
		{
			if(s.length) s += ' ';
			s += css_sz(args[i]);
			if(ispx) s += 'px';
		}
		return s;
	}

})

extjsf.Bind.extend(
{
	getPropsNode     : function(node_id)
	{
		if(!node_id && this.$node_id())
			node_id = this.$node_id() + '-props';
		return node_id && Ext.getDom(node_id);
	},

	isPropsNode      : function(node_id)
	{
		var node = this.getPropsNode(node_id);
		var text = node && node.innerHTML;

		if(ZeT.iss(text)) text = ZeTS.trim(text);
		return !!(text && text.length);
	},

	nodeId           : function(nodeId)
	{
		if(!ZeT.iss(nodeId))
			return this.$node_id()
		this._node_id = nodeId
		return this;
	},

	value            : function(v)
	{
		var c = this.co()
		var p = this.$raw()

		if(ZeT.isu(v) || (v === null))
			return (c && ZeT.isf(c.getValue))?(c.getValue()):(p.value);

		if(c && ZeT.isf(c.setValue)) c.setValue(v)
		else p.value = v;

		return this;
	},

	visible          : function(v)
	{
		var c = this.co()
		var p = this.$raw()

		if(ZeT.isu(v) || (v === null))
			return (!c || !ZeT.isf(c.isVisible))?(undefined):(c.isVisible())

		if(!c) p.hidden = !v; else
			if(ZeT.isf(c.setVisible)) c.setVisible(!!v)

		return this
	},

	clearComponent   : function(opts)
	{
		var c = this.co(); if(!c) return this;

		//~: remove the children
		c.removeAll()

		//~: clear all the listeners
		if(!opts || !opts.notListeners)
			c.clearListeners()

		//~: remove docked panels
		var d = c.getDockedItems('component[dock]');
		for(var i = 0;(i < d.length);i++)
		{
			//ZeT.log('remove dock ', d[i].id)
			if(d[i].extjsfBind) c.removeDocked(d[i])
		}

		return this;
	},


	//=     Form Components      =//

	/**
	 * Converts the children binds to the items
	 * (descriptors) of child components in Ext JS.
	 */
	extjsItems       : function()
	{
		return this._children(this._items);
	},

	addItem          : function(bind)
	{
		if(ZeT.iss(bind)) bind = extjsf.bind(bind);
		if(bind) this._items.push(bind)
		return this;
	},

	replaceItems     : function(items)
	{
		var old = this._items;
		this._items = ZeT.isa(items)?(items):[];
		return old;
	},

	hasItems         : function()
	{
		return !!this._items.length;
	},

	hasHTML          : function()
	{
		var html = this.$raw['html']
		return ZeT.iss(html) || ZeT.isDelayed(html)
	},

	toggleReadWrite  : function(isread)
	{
		ZeT.assertn(this.co())
		var collect = []; ZeT.each(this.co().query(), function(f)
		{
			if(f.extjsfReadWrite !== true) return

			//?: {read-write}
			if(ZeT.isf(f.setReadOnly))
				f.setReadOnly(isread)
			else if(ZeT.isf(f.setDisabled))
				f.setDisabled(isread)
			else
				return

			collect.push(f)
		})

		return collect
	},

	toggleBlock      : function(block)
	{
		ZeT.asserts(block)

		//~: access the component' children
		var c = this.co()
		if(c) c.items.each(function(c)
		{
			if(!ZeT.iss(c.extjsfBlock)) return
			c.setVisible(c.extjsfBlock == block)
		})

		//?: {has no component yet}
		if(!c) ZeT.each(this._items, function(i)
		{
			ZeT.assert(i.extjsfBind === true)
			if(!ZeT.iss(i._props.extjsfBlock)) return
			i._props.hidden = (i._props.extjsfBlock != block)
		})
	},

	_children        : function(chs)
	{
		var res = [];

		var x = false;

		for(var i = 0;(i < chs.length);i++)
		{
			if(chs[i].extjsfRawItem)
			{
				res.push(chs[i])
				continue;
			}

			if(ZeT.isf(chs[i].co) && chs[i].co())
			{
				res.push(chs[i].co())
				continue;
			}

			var item  = {};
			var props = chs[i].buildProps()
			var keysl = ZeT.keys(item).length;
			item = ZeT.extend(item, props || item);

			//!: add the item as the child
			res.push(ZeT.extend(item, props || item))
		}

		return res;
	},

	buildProps       : function()
	{
		var res  = this.$raw()
		var self = this

		//~: get value from the DOM node
		var node = this.$node_id() && Ext.get(this.$node_id())
		if(!res.value && node) //!: deprecated
		{
			var value = node.getAttribute('value');
			if(value && value.length)
				res.value = value;
		}

		//~: assign render to
		if(this._render_to)
			res.renderTo = this._render_to;

		//~: created children (definitions)
		if(!res.items)
		{
			var items = this.extjsItems();

			if(ZeT.isa(items) && items.length)
				res.items = items;
		}

		if(res.items) for(var i = 0;(i < res.items.length);i++)
			if(res.items[i].extjsfRawItem)
				delete res.items[i].extjsfRawItem

		//~: resolve delayed properties
		ZeT.undelay(res)

		//~: merge the listeners
		res.listeners = this._xlisteners(res.listeners);

		//~: the handler
		if(ZeT.isf(this.handler))
			res.handler = ZeT.assertf(this.handler)

		//?: {is data store bound}
		if(this.store) if(!this.store.createStore)
			res.store = this.store;
		else
		{
			this.store.createStore = undefined;
			this.store = res.store =
			  Ext.create('Ext.data.Store', this.store);
		}

		//!: extjsf Bind reference
		res.extjsfBind = this;

		return res;
	},

	//=        Listeners         =//

	listeners        : function()
	{
		var funmap = this._listeners;
		var result = {};
		var fmkeys = ZeT.keys(funmap);

		for(var i = 0;(i < fmkeys.length);i++)
		{
			var ename = fmkeys[i];
			if(!ZeT.iss(ename) || !ZeT.isa(funmap[ename])) continue;

			this._bind_on(ename, result)
		}

		//!: always support for component 'added' and destroy event
		this._bind_on('added', result)
		this._bind_on('destroy', result)

		return result;
	},

	_xlisteners      : function(external)
	{
		var result = this.listeners();
		if(!external) return result;

		//c: for all external listeners
		var l, ls, n, ns = ZeT.keys(external);
		for(var i = 0;(i< ns.length);i++)
		{
			if(!ZeT.iss(n = ns[i])) continue;

			//~: bind local event handler
			this._bind_on(n, result)

			//~: update listeners map
			if(!(ls = this._listeners[n]))
				this._listeners[n] = ls = [];

			//?: {external listener is a function}
			if(ZeT.isf(l = external[n]))
			{
				//?: {it is not the on-handler} add it
				if(!l.extjsfBindOn) ls.push(l)
				continue;
			}

			//!: it is Ext JS listener definition object

			//~: swap on-handler with object
			var fn = l.fn; l.fn = result[n]; result[n] = l;

			//?: {fn is not the on-handler} add it
			if(ZeT.isf(fn) && !fn.extjsfBindOn) ls.push(fn)
		}

		return result;
	},

	on               : function(ename, func)
	{
		ZeT.assert(ZeT.iss(ename))
		ZeT.assert(ZeT.isf(func))

		if(this.co())
		{
			this.co().removeListener(ename, func)
			this.co().on(ename, func)
		}
		else
		{
			var list = this._listeners[ename]
			if(!list) this._listeners[ename] = list = []

			if(list.indexOf(func) == -1)
				list.push(func)
		}

		return this
	},

	on_create        : function(co)
	{
		this._component = co
		co.extjsfBind = this
	},

	/**
	 * This implementation tries to handle properly two
	 * cases: when component is solely destroyed, or
	 * it's being destroyed with the Domain.
	 */
	on_destroy       : function()
	{
		delete this._component

		//?: {is not registered}
		if(!ZeT.iss(this.name)) return
		ZeT.assert(ZeT.iss(this.domain))

		var self = this, domain = extjsf.domain(this.domain)
		if(domain) ZeT.timeout(2000, function()
		{
			//?: {still have this bind registered}
			var bind = domain.bind(self.name)
			if(bind == self) domain.unbind(self.name, false)
		})
	},

	_on              : function()
	{
		var args   = ZeT.a(arguments);
		var ename  = args.shift();

		//?: {added event} set the component
		if(ename === 'added')
			this.on_create(args[0])

		var functs = this._listeners[ename] || [];
		var target = this._component || this;

		//HINT: we return the first defined result!

		var result = undefined;
		for(var i = 0, l = functs.length;(i < l);i++)
		{
			var r = functs[i].apply(target, args);
			if(!ZeT.isu(r) && ZeT.isu(result)) result = r;
		}

		//?: {before destroy} unbind
		if(ename === 'destroy')
			this.on_destroy(args[0])

		return result
	},

	_bind_on         : function(ename, res)
	{
		if(res && res[ename]) return undefined;

		var f = ZeT.fbind(this._on, this, ename);
		f.extjsfBindOn = true;

		if(res) res[ename] = f;
		return f;
	}
})


extjsf.WinmainLoader = ZeT.defineClass('extjsf.WinmainLoader',
{
	init             : function(domain)
	{
		if(!ZeT.iss(domain)) throw 'Can not create ' +
		  'window loader as no ExtJSF domain is defined!';

		this._domain = domain;
		this._params = {};
	},

	url              : function(url)
	{
		if(!ZeT.iss(url)) return this;
		this._url = url;
		return this;
	},

	params           : function(params)
	{
		if(!params) return this._params;
		this._params = params;
		return this;
	},

	addParams        : function(params)
	{
		ZeT.extend(this._params, params)
		return this;
	},

	form             : function(form)
	{
		if(!form) return this._form;
		this._form = form;
		return this;
	},

	button           : function(button)
	{
		if(!button) return this._button;
		this._button = button;
		return this;
	},

	setMethod        : function(m)
	{
		this._method = m;
		return this;
	},

	load             : function()
	{
		//~: find the main window
		var winmain = extjsf.bind('winmain', this._domain);
		if(!winmain) throw 'Can not find winmain in ' +
		  'the domain: [' + this._domain + ']!';

		//~: get the support form
		var supform = this._form && Ext.get(this._form);

		//?: {has no specific url} take support form action
		if(!this._url && supform)
			this._url = supform.getAttribute('action');

		//?: {has no action url}
		if(!this._url || !this._url.length)
			throw 'Can not reload winmain with undefined URL!';

		//~: collect the parameters
		var prms = ZeT.extend({}, this._params);
		if(supform) this._form_params(supform, prms, this._button)

		//~: resolve delayed parameters
		ZeT.undelay(prms)

		//~: cleanup the domain
		if(extjsf.domain(this._domain))
			extjsf.domain(this._domain).destroy({ except: [ winmain ]})

		//~: create the domain with this window
		extjsf.domain(this._domain).bind('winmain', winmain)

		//~: clear the component
		this._clear(winmain)

		//~: set temporary title
		winmain.co().setTitle('Выполняется запрос...')

		var adr = this._url;
		var mth = ZeT.iss(this._method)?(this._method):
		  (supform)?('POST'):('GET');

		//~: bind the domain
		prms.domain = this._domain;

		//!: reload content
		Ext.create('Ext.ComponentLoader', {
		  target: winmain.co(), url: adr,
		  ajaxOptions: { method: mth, timeout: 30 * 60 * 1000 }, //<-- 30 min
		  params: prms, autoLoad: true, scripts: true
		})
	},

	_clear           : function(winmain)
	{
		//~: clear the window
		winmain.clearComponent({notListeners: true})

		//~: rebind domain deleter
		//if(winmain._domain_deleter)
		//	winmain.on('beforedestroy', winmain._domain_deleter)
	},

	_form_params     : function(form, prms, button)
	{
		var inputs = form.select('input');

		if(inputs) for(var i = 0;(i < inputs.getCount());i++)
		{
			var item  = inputs.item(i);            if(!item) continue;
			var name  = item.getAttribute('name'); if(!name) continue;
			var value = item.getAttribute('value') || '';

			//?: {this field is a submit button} skip it
			if(item.getAttribute('type') == 'submit')
				if(!(button === name) && !(button === true))
					continue;

			prms[name] = value;
		}
	}
})


ZeT.init('extjsf: data models', function()
{
	Ext.syncRequire('Ext.data.Model')

	//~: data model for simple drop-lists
	Ext.define('extjsf.model.BasicDropList', {
	  extend: 'Ext.data.Model',

	  idProperty: 'key',

	  fields: [

	    {name: 'key',   type: 'string'},
	    {name: 'label', type: 'string'}
	  ]
	})
})


extjsf.support = ZeT.singleInstance('extjsf.support', {

	gridColumns            : function(grid, visible)
	{
		if(grid.extjsfBind === true) grid = grid.co();
		return (visible)?(grid.headerCt.getVisibleGridColumns()):
		  (grid.headerCt.getGridColumns());
	},

	columnIndexByDataIndex : function(grid, dataIndex)
	{
		var columns = ZeT.isa(grid)?(grid):(this.gridColumns(grid));

		for(var i = 0;(i < columns.length);i++)
			if(columns[i].dataIndex === dataIndex)
				return i;

		return undefined;
	},

	columnByDataIndex      : function(grid, dataIndex)
	{
		var columns = ZeT.isa(grid)?(grid):(this.gridColumns(grid));

		for(var i = 0;(i < columns.length);i++)
			if(columns[i].dataIndex === dataIndex)
				return columns[i];

		return undefined;
	},

	columnByType           : function(grid, xtype)
	{
		var columns = ZeT.isa(grid)?(grid):(this.gridColumns(grid));

		for(var i = 0;(i < columns.length);i++)
			if(columns[i].xtype === xtype)
				return columns[i];

		return undefined
	},

	refreshGridIndices     : function(grid)
	{
		if(ZeT.isa(grid))
			return Ext.Array.forEach(grid, function(i) {i.index = null})

		var store = (grid.isStore === true)?(grid):(grid.getStore())
		store.each(function(i) {i.index = null})
		if(ZeT.isf(grid.getView)) grid.getView().refresh()
	},

	/**
	 * Moves currently selected items of the grid up or
	 * down with optional callback notifier. The delay
	 * timeout is optional and 1000 ms by the default.
	 *
	 * Returns false when no updates took place.
	 */
	moveGridSelected       : function(up, grid, delay_fn, fn)
	{
		var b = extjsf.asbind(grid), g = extjsf.co(grid);

		//~: selected items
		var s = g.getSelectionModel().getSelection();
		if(!s || !s.length) return false

		//?: {can't move up}
		if(up && (g.getStore().indexOf(s[0]) == 0))
			return false

		//?: {can't move down}
		if(!up && (g.getStore().indexOf(s[s.length - 1]) + 1 == g.getStore().getCount()))
			return false

		//~: deselect
		g.getSelectionModel().deselectAll()

		//~: move the selected up-down
		Ext.Array.forEach(s, function(x)
		{
			var i = g.getStore().indexOf(x);

			if( up) ZeT.assert(i     > 0)
			if(!up) ZeT.assert(i + 1 < g.getStore().getCount())

			g.getStore().removeAt(i)
			if( up) g.getStore().insert(i - 1, [x])
			if(!up) g.getStore().insert(i + 1, [x])
		})

		//~: select items back
		g.getSelectionModel().select(s)

		//~: update delay
		var dl = ZeT.isn(delay_fn)?(delay_fn):(1000);
		    fn = ZeT.isf(delay_fn)?(delay_fn):ZeT.isf(fn)?(fn):(null);
		if(!fn) return; //?: {has no callback}

		//~: update on the server with the delay
		if(!b.updating) b.updating = 1;
		var ui = ++b.updating;

		ZeT.timeout(dl, function()
		{
			//?: {has no more update requests}
			if(ui === b.updating) fn(grid)
		})
	},

	collapseTreeToSel      : function()
	{
		var tree = extjsf.co.apply(extjsf, arguments)
		var sel  = tree.getSelectionModel().getSelection()
		sel = sel.length && sel[0]

		tree.collapseAll(function()
		{
			if(sel) while(sel.parentNode)
			{
				sel.parentNode.expand()
				sel = sel.parentNode
			}
		})
	}
})