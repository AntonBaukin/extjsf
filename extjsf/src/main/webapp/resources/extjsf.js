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
	 * ExtJS component is being destroyed!
	 */
	unbind           : function(name, destroy)
	{
		ZeT.asserts(name, 'Can not lookup a Bind by ',
		  'not a string name: ', name)

		//~: remove the bind
		var bind = this.binds.remove(name)
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
		this._listeners   = {}
		this._items       = []
		this._extjs_props = {}

		//WARNING: this prevents recursion in Ext.clone()!
		this.constructor  = null
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
	 * Tells JSF component ID and optional ExtJSF
	 * component ID that by default is the bind name.
	 */
	ids              : function(clientId, coid)
	{
		//~: JSF component id
		this._client_id = ZeT.asserts(clientId)

		//~: unique ExtJSF component id
		this.coid = !ZeT.ises(coid)?(coid):ZeT.asserts(this.name)

		//~: ExtJS component id
		this.props({ id: this.coid })

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
	 */
	parent           : function(parent_coid, target_coid)
	{
		//~: default parent
		if(!ZeT.ises(parent_coid))
			this._parent_coid = parent_coid

		//~: targeted parent
		if(!ZeT.ises(target_coid))
			this._target_coid = target_coid

		return this
	},

	/**
	 * Tells DOM node ID to render this component to.
	 * Optional 'parent' arguments tells the name of
	 * the Bind (of the same domain) to attach this
	 * component to. This allows to destroy this
	 * component with the target one.
	 */
	renderTo         : function(nodeid, parent)
	{
		if(ZeT.ises(nodeid))
			return this

		//~: render node id
		this._render_to = ZeTS.trim(nodeid)

		//?: {has render parent}
		if(!ZeT.ises(parent))
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
		//?: {is not rendering to some else place}
		if(ZeT.ises(this._render_to) && !this._target_coid)
			this.$install()

		return this
	},

	/**
	 * Invoked always after the JSF facets with the
	 * nested components are rendered, and always
	 * after the install. Does all required the
	 * component to work.
	 *
	 * Note that at this point ExtJS components
	 * are still might be not available!
	 */
	render           : function()
	{
		var self = this

		//?: {render to node}
		if(!ZeT.ises(this._render_to))
			Ext.onReady(function()
			{
				//~: create the component
				if(!self.co())
					self.co(self.$create())

				//?: {has component render parent}
				if(self._render_parent)
					self.renderParent()
				//?: {has default render parent}
				else if(!self._target_coid)
					extjsf.bind(self._target_coid, self.domain).
					  on('beforedestroy', this.boundDestroy())
			})

		//?: {add to the container component}
		else if(this._target_coid)
			extjsf.bind(this._target_coid, this.domain).on('added', function(parent)
			{
				//~: create the component
				if(!self.co())
					self.co(self.$create())

				//~: append to the parent
				parent.add(self.co())
			})
	},

	/**
	 * Invokes ZeT.scope() giving the first
	 * argument this Bind instance.
	 */
	scope            : function()
	{
		ZeT.assertf(f)

		//~: argument this bind
		var a = ZeT.a(arguments)
		a.splice(0, 0, this)

		return ZeT.scope.apply(ZeT, a)
	},

	/**
	 * Private. Creates the component.
	 */
	$create          : function()
	{
		return Ext.ComponentManager.create(this.props())
	},

	$install         : function()
	{
		if(!this._parent_coid)
			return this

		var d = ZeT.assertn(extjsf.domain(this.domain),
		  'This Bind is not registered in any Domain!')

		var p = ZeT.assertn(d.bind(this._parent_coid),
		  'Parent Bind [', this._parent_coid, '] is not',
		  ' found in the Domain [', this.domain, ']!')

		p.addItem(this) //!: add this bind as a child
	},

	/**
	 * Private. Returns ID of DOM node that
	 * stores the properties of this component.
	 */
	$node_id         : function()
	{
		if(!ZeT.ises(this._node_id))
			return this._node_id

		//?: {form from the client id}
		if(!ZeT.ises(this._client_id))
			return ZeTS.cat(this._client_id, '-', ZeT.asserts(this.coid))
	}
})




// +----: Components to Refactor :------------------------------->


ZeT.extend(extjsf,
{
	//=    Components Binding    =//

	/**
	 * Tries to get the ExtJS component from
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
	 * Assigns handler function of a bind, or component.
	 * Supported arguments are (check extjsf.asbind()):
	 *
	 *  · name, domain, handler
	 *
	 *  · bind or component, handler
	 *
	 */
	bindHandler      : function()
	{
		var bind    = extjsf.asbind.apply(ZeT, arguments)
		var comp    = bind && bind.co()
		var handler = ZeT.isf(arguments[2])?(arguments[2]):
		  ZeT.isf(arguments[1])?(arguments[1]):(null)

		if(bind && ZeT.isf(handler))
		{
			bind.handler = handler
			if(comp && ZeT.isf(comp.setHandler))
				comp.setHandler(handler)

			return this
		}

		if(bind && !ZeT.isf(bind.handler) && comp)
			bind.handler = comp.handler

		return bind && bind.handler
	},

	xbindHandler     : function()
	{
		var h = extjsf.bindHandler.apply(extjsf, arguments)
		return h || Ext.emptyFn
	},

	handlerCaller    : function(name, domain)
	{
		return function()
		{
			var f = extjsf.bindHandler(name, domain);
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
	 * 'http://tverts.com/retrade/webapp/response' XMLs.
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
	co               : function(component)
	{
		if(ZeT.isu(component)) return this._component;
		this._component = component;
		return this;
	},

	raw    : function()
	{
		return this._extjs_props;
	},

	props       : function(props)
	{
		if(!props) return this.$props();

		if(ZeT.iss(props))
			props = this._evalProps(props);

		ZeT.extend(this._extjs_props, props)
		return this;
	},

	readPropsNode   : function(node_id)
	{
		var props = this.evalPropsNode(node_id);
		if(props) this.props(props)
		return this;
	},

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

	prependId        : function(local_id)
	{
		if(!ZeT.iss(this._node_id)) throw 'Can not prepend local ID [' +
		  local_id + '] as there is no Node ID bound the ExtJSF component!';

		//NOTE: JSF prepend separator '-' must be configured in web.xml!
		return this._node_id.concat('-', local_id);
	},

	value            : function(v)
	{
		var c = this.co();
		var p = this._extjs_props;

		if(ZeT.isu(v) || (v === null))
			return (c && ZeT.isf(c.getValue))?(c.getValue()):(p.value);

		if(c && ZeT.isf(c.setValue)) c.setValue(v)
		else p.value = v;

		return this;
	},

	visible          : function(v)
	{
		var c = this.co()
		var p = this._extjs_props

		if(ZeT.isu(v) || (v === null))
			return (!c || !ZeT.isf(c.isVisible))?(undefined):(c.isVisible())

		if(!c) p.hidden = !v; else
			if(ZeT.isf(c.setVisible)) c.setVisible(!!v)

		return this
	},

	/**
	 * For the given parent bind id, bind,
	 * or component updates the component of
	 * this bind (or will update) to behave
	 * as it is nested in that parent, but
	 * rendered somewhere else.
	 *
	 * Note that this also binds the destroy.
	 * This function may be invoked only once!
	 */
	renderParent     : function()
	{
		//?: {is not rendered to}
		if(ZeTS.ises(this._render_to))
			return this

		//~: access the parent bind
		var p; if(ZeT.iss(p = this._render_parent))
		{
			p = extjsf.asbind(p, this.domain)

			ZeT.assert(extjsf.isbind(p), 'Bind of the render parent [',
			  this._render_parent, '] is not found at [', this.domain, ']!')

			this._render_parent = p
		}

		ZeT.assert(extjsf.isbind(p))

		//?: {invoked more than once}
		if(this._rendered_parent)
			ZeT.assert(p === this._render_parent)
		this._render_parent = p
		this._rendered_parent = true

		//?: {destroy is not connected yet}
		if(!this._render_parent_destroy)
		{
			this._render_parent_destroy = true
			p.on('beforedestroy', this.boundDestroy())
		}

		//?: {has no own component yet}
		if(!this.co()) //<-- invoked later
			return this

		//?: {has no parent component yet}
		if(!p.co())    //<-- invoked later
		{
			var self = this; p.on('added', function()
			{
				self.renderParent()
			})

			return this
		}

		delete this._render_parent
		this.$render_parent(p.co())
		return this
	},

	$render_parent   : function(p)
	{
		var co = this.co()

		//--> up()
		//  used when component is rendered in menu,
		//  or client event will close the menu before
		//  clicking the component!
		var up = co.up
		co.up = function(selector)
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

	evalPropsNode    : function(node_id)
	{
		var node = this.getPropsNode(node_id);
		var text = ZeTX.text(node);
		return ZeTS.ises(text)?({}):(this._evalProps(text));
	},

	_evalProps       : function(props)
	{
		props = ZeTS.trim(props);
		if(ZeTS.first(props) !== '{')
			props = '{'.concat(props, '}');
		if(ZeTS.first(props) !== '(')
			props = '('.concat(props, ')');

		return eval(props);
	},

	destroy          : function(opts)
	{
		//?: {skip this component}
		if(opts && ZeT.isa(opts.except) && (opts.except.indexOf(this) != -1))
			return

		var co; if(co = this.co()) try
		{
			co.destroy()
		}
		finally
		{
			delete this._component
		}
	},

	boundDestroy     : function()
	{
		return ZeT.fbind(this.destroy, this)
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
		var html = this._extjs_props['html'];
		return ZeT.iss(html) || ZeT.isDelayed(html);
	},

	goFormAction     : function(nodeid, action)
	{
		if(ZeTS.ises(nodeid)) nodeid = this.$node_id();
		if(ZeTS.ises(nodeid)) return this;

		var node = Ext.get(nodeid);
		if(!node || (node.dom.tagName.toLowerCase() != 'form')) return this;

		if(ZeTS.ises(action))
			action = node.getAttribute('action');
		if(ZeTS.ises(action)) return this;

		var prefix = ''; if(action.charAt(0) == '/')
		{
			prefix = '/';
			action = action.substring(1);
		}

		var i = action.indexOf('/'); if(i != 0)
		{
			prefix += action.substring(0, i + 1);
			action =  action.substring(i + 1);
		}

		if(prefix.charAt(prefix.length - 1) != '/') prefix += '/';
		if(ZeTS.ends(action, '.xhtml'))
			action = action.substring(0, action.length - 6);

		node.set({'action': prefix + 'go/' + action})
		return this;
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
			if(!ZeT.iss(i._extjs_props.extjsfBlock)) return
			i._extjs_props.hidden = (i._extjs_props.extjsfBlock != block)
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
			var props = chs[i].$props();
			var keysl = ZeT.keys(item).length;
			item = ZeT.extend(item, props || item);

			//!: add the item as the child
			res.push(ZeT.extend(item, props || item))
		}

		return res;
	},

	$props      : function()
	{
		var res  = this._extjs_props;
		var node = this.$node_id() && Ext.get(this.$node_id());
		var self = this;

		//~: get value from the DOM node
		if(!res.value && node)
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
			res.handler = this.handler;

		//?: {is menu bound}
		if(this.menu && this.menu.extjsfBind)
			res.menu = this.menu.co();

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


	//=       Form Submit        =//

	bindSubmitForm   : function(opts)
	{
		return ZeT.fbind(this.submitForm, this, opts);
	},

	/**
	 * Set form before-submit validator invoked as:
	 *
	 *   v.call(bind, form, opts)
	 *
	 * Validation fails when callback returns false.
	 */
	validator        : function(v)
	{
		this._validator = undefined;
		if(ZeT.isf(v)) this._validator = v;

		return this;
	},

	submitForm       : function(opts)
	{
		opts = opts || {};

		var jsf_form = this.$node_id() && Ext.get(this.$node_id());
		var ext_form = this.co() &&
		  this.co().getForm && this.co().getForm();
		if(!ext_form) throw 'Can not issue form submit as no form is found!';

		//~: {validate the form}
		if(!this._validator && !ext_form.isValid())
			return false;
		else if(this._validator)
			if(false === this._validator.call(this, ext_form, opts))
				return false;

		//~: detect what fields do present in Ext JS form
		var ext_flds = {};
		ext_form.getFields().each(function(f) {
			ext_flds[f.getName()] = true;
		})

		//~: collect the (hidden) parameters present in JSF form only
		var jsf_inps = jsf_form && jsf_form.select('input');
		var jsf_prms = opts.params || {};

		//~: define JSF command action
		var jsf_cmd  = null;
		if(ZeT.iss(opts.command)) jsf_cmd = this.prependId(opts.command);

		if(jsf_inps) for(var i = 0;(i < jsf_inps.getCount());i++)
		{
			var item  = jsf_inps.item(i);          if(!item) continue;
			var name  = item.getAttribute('name'); if(!name) continue;
			var value = item.getAttribute('value') || '';

			//?: {this field is a submit button}
			if(item.getAttribute('type') == 'submit')
				//?: {it is not the command requested} skip this submit
				if(name !== jsf_cmd) continue;

			//?: {the field does not present in Ext JS form}
			if(!ext_flds[name]) jsf_prms[name] = value;
		}

		//~: detect URL
		var jsf_url = jsf_form && jsf_form.getAttribute('action');
		if(!jsf_url || !jsf_url.length) jsf_url = window.location.toString();

		//~: set view mode parameter
		jsf_prms.mode = opts.mode || 'BODY_POST';

		//~: set the domain
		jsf_prms.extjs_domain = opts.domain || this.domain;

		var bind = this;

		//!: do submit
		ext_form.submit({ url : jsf_url, params : jsf_prms,
		  success : ZeT.fbind(bind._validate, bind, opts),
		  failure : ZeT.fbind(bind._call_failure, bind, opts),
		  clientValidation: false //<-- used special validation procedure
		})

		return this;
	},

	/**
	 * Register the callback on success Ajax operation.
	 * The context of the callback call is always this
	 * bind instance.
	 *
	 * The arguments of the callback are:
	 *
	 *  · [this]  this Bind;
	 *  · opts    the options given;

	 *  · data    the data returned from the server.
	 *    (XML Document in the most cases.)
	 *
	 *  · action  ExtJS action;
	 *  · form    ExtJS form.
	 */
	success          : function(func)
	{
		if(!ZeT.isf(func)) return this._on_success;
		this._on_success = func;
		return this;
	},

	failure          : function(func)
	{
		if(!ZeT.isf(func)) return this._on_failure;
		this._on_failure = func;
		return this;
	},

	_validate        : function(opts, form, action)
	{
		if(!action.result) throw 'ExtJS submit action returns no response!';

		var success = true;

		//~: display errors of the fields
		if(ZeT.isa(action.result.errors) && action.result.errors.length)
		{
			var ers = action.result.errors;
			for(var i = 0;(i < ers.length);i++) if(!ZeTS.ises(ers[i].target))
			{
				var cmp = Ext.getCmp(ers[i].target);

				if(!cmp)
				{
					var bind = extjsf.bind(ers[i].target, this.domain);
					if(bind) cmp = bind.co();
				}

				if(cmp && cmp.isFormField)
					cmp.markInvalid(ers[i].error)
			}

			success = false;
		}

		//~: read success status from the attribute
		var xml = form.errorReader && form.errorReader.rawData; if(xml)
		{
			var status = ZeTX.attr(ZeTX.node(xml, 'validation'), 'success');
			if(status) success = ('true' === status);
		}

		if(success)
			this._call_success(opts, xml, action, form)
		else
			this._call_failure(opts, xml, action, form)
	},

	_call_success    : function(opts, xml)
	{
		//~: evaluate the scripts
		this._call_scripts(opts, xml)

		if(ZeT.isf(this._on_success))
			this._on_success.apply(this, arguments)

		if(ZeT.isf(opts.success))
			opts.success.apply(this, arguments)
	},

	_call_failure    : function(opts, xml)
	{
		//~: evaluate the scripts
		this._call_scripts(opts, xml)

		if(ZeT.isf(this._on_failure))
			this._on_failure.apply(this, arguments)

		if(ZeT.isf(opts.failure))
			opts.failure.apply(this, arguments)
	},

	_call_scripts    : function(opts, xml)
	{
		var scripts = ZeTX.nodes(xml, 'script')
		if(scripts) for(var i = 0;(i < scripts.length);i++) try
		{
			eval('(function() {'.concat(ZeTX.text(scripts[i]), '})()'))
		}
		catch(e)
		{
			ZeT.log(e)
		}
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
		this._bind_on('beforedestroy', result)

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

	on_create        : function(component)
	{
		this._component = component
		component.extjsfBind = this

		//?: {has render parent}
		if(this._render_parent)
			this.renderParent()
	},

	/**
	 * This implementation tries to handle properly two
	 * cases: when component is solely destroyed, or
	 * it's being destroyed with the Domain.
	 */
	on_destroy       : function(component)
	{
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
		if(ename === 'beforedestroy')
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
		prms.extjs_domain = this._domain;

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