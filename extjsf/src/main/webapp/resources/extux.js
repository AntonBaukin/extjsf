/*===============================================================+
 |                                                               |
 |   Ext JS User Extensions                                      |
 |                                   / anton.baukin@gmail.com /  |
 +===============================================================*/


/**
 * Default timeout to 30 minutes.
 */
Ext.Ajax.setTimeout(1000 * 60 * 30)

/**
 * Forbid sending '_dc' HTTP parameter.
 */
Ext.Loader.setConfig({ disableCaching: false })
Ext.Ajax.setConfig({ disableCaching: false })
Ext.data.proxy.Server.$config.add({ noCache: false })

/**
 * Make window shadow to be on all the sides.
 */
Ext.window.Window.override(
{

	shadow            : 'frame',

	privates          :
	{
		/**
		 * In present implementation we don't align
		 * floating windows to any scroll!
		 */
		onAlignToScroll: function ()
		{
			this.clearAlignEl()
		}
	},

	onEsc             : function()
	{
		if(ZeT.isf(this['onEscAlt']))
			return this['onEscAlt'].apply(this, arguments)
	}
})

/**
 * Override scripts execution function
 * to catch up scripts having errors.
 */

window._exec_script = window._exec_script || window.execScript || window.eval
window.execScript   = function()
{
	try
	{
		return window._exec_script.apply(this, arguments)
	}
	catch(e)
	{
		if(ZeT) ZeT.log('Caught unhandled exception: ', this, arguments, e)
		else if(console && typeof console.log === 'function')
			console.log('Unhandled script evaluation: ', this, arguments, e)

		throw e
	}
}

/**
 * Ext JS FIX: Component.setHtml()
 */
Ext.Component.override({

	setHtml           : function()
	{
		this.update.apply(this, arguments)
	},

	fireEventArgs     : function()
	{
		try
		{
			return this.callParent(arguments)
		}
		catch(e)
		{
			if(ZeT) ZeT.log('Caught unhandled exception: ', this, arguments, e)
			else if(console && typeof console.log === 'function')
				console.log('Unhandled script evaluation: ', this, arguments, e)
		}
	}
})

Ext.mixin.Observable.override(
{
	fireEventArgs     : function()
	{
		try
		{
			return this.callParent(arguments)
		}
		catch(e)
		{
			if(ZeT) ZeT.log('Caught unhandled exception: ', this, arguments, e)
			else if(console && typeof console.log === 'function')
				console.log('Unhandled script evaluation: ', this, arguments, e)
		}
	}
})

Ext.form.field.ComboBox.override(
{
	/**
	 * ComboBox default list options.
	 */
	defaultListConfig : {

		loadingHeight  : 64,
		minWidth       : 64,
		maxHeight      : 342,
		shadow         : 'sides'
	}
})

Ext.form.field.Picker.override(
{
	/**
	 * Makes the picker to be not smaller than the field.
	 */
	expand            : function()
	{
		if(this.rendered && !this.isExpanded && !this.isDestroyed)
		{
			var picker = this.getPicker()

			if(picker && !this.matchFieldWidth && (this.pickerWidthAuto === true))
				if(!ZeT.isn(picker.width) || (picker.width < this.bodyEl.getWidth()))
					picker.width = this.bodyEl.getWidth()
		}

		return this.callParent(arguments)
	}
})

Ext.resizer.Splitter.override(
{
	/**
	 * We don't need this not to confuse a user.
	 */
	collapseOnDblClick : false,

	setDisabled        : function(disabled)
	{
		if(this.tracker) if(disabled)
			this.tracker.disable()
		else
			this.tracker.enable()

		return this.callParent(arguments);
	}
})


// +----: Time Field + Picker :----------------------------------+

Ext.define('Ext.ux.form.field.Time',
{
	extend            : 'Ext.form.field.Time',
	alias             : 'widget.ux.timefield',

	initComponent     : function()
	{
		this.callParent(arguments)

		//~: create store using the picker
		var store = this.getPicker().
		  createStore(this.format, this.increment)

		this.bindStore(store, true, true)
		this.getPicker().setSelection(
		  this.findRecordByValue(this.getValue()))
	},

	createPicker      : function()
	{
		if(!this.listConfig)
			this.listConfig = {}
		if(!this.listConfig.xtype)
			this.listConfig.xtype = 'ux.timepicker'

		return this.callParent(arguments)
	},

	isEqual           : function(v1, v2)
	{
		v1 = Ext.Array.from(v1)
		v2 = Ext.Array.from(v2)

		if(v1.length != v2.length)
			return false

		var FMT = this.getPicker().format
		function tstr(t)
		{
			if(t instanceof Date)
				t = Ext.Date.dateFormat(t, FMT)

			ZeT.asserts(t)
			return t
		}

		for(var i = 0;(i < v1.length); i++)
			if(tstr(v2[i]) != tstr(v1[i]))
				return false

		return true
	},

	findRecordByValue : function(v)
	{
		if(v instanceof Date)
			v = Ext.Date.dateFormat(v, this.getPicker().format)
		ZeT.asserts(v)

		var m = null; this.store.each(function(x)
		{
			if(x.get('disp') == v) { m = x; return false }
		})

		return m
	}
})


Ext.define('Ext.ux.picker.Time',
{
	extend            : 'Ext.picker.Time',
	alias             : 'widget.ux.timepicker',

	createStore       : function()
	{
		var me   = this, times = []
		var DU   = Ext.Date
		var TP   = Ext.picker.Time.prototype
		var ID   = TP.initDate
		var min  = DU.clearTime(new Date(ID[0], ID[1], ID[2]))
		var max  = DU.add(min, 'mi', (24 * 60) - 1)
		var time = (!me.pickerField)?(null):(me.pickerField.getValue())

		function fmt(t)
		{
			return DU.dateFormat(t, me.format)
		}

		function add(t)
		{
			times.push({date: t, disp: fmt(t)})
		}

		while(min <= max)
		{
			var skip = false

			//?: {has present time value to insert}
			if(time)
			{
				skip = (fmt(time) == fmt(min))

				//?: {add this time}
				if(skip || (min > time))
				{
					add(time)
					time = null
				}
			}

			if(!skip) add(min)
			min = DU.add(min, 'mi', me.increment)
		}

		return new Ext.data.Store({ data: times, model: TP.modelType })
	}
})


// +----: Detached Button :--------------------------------------+

/**
 * This special class is for buttons with render-to in menus.
 * In Ext JS 5.1 mouse pressing on them hides the menu before
 * the click event occurs.
 */
Ext.define('Ext.ux.button.Detached',
{
	extend            : 'Ext.button.Button',
	alias             : 'widget.ux.button-detached',

	onMouseDown       : function(e)
	{
		e.stopEvent()
		return this.callParent(arguments)
	}
})


// +----: Tri-State Checkbox :-----------------------------------+

/**
 * Check Box having three states (in the toggle order):
 *   null-checked, unchecked, and checked.
 */
Ext.define('Ext.ux.form.TriCheckbox',
{
	extend            : 'Ext.form.field.Checkbox',

	alias             : 'widget.ux.tricheckbox',

	xchecked          : false,
	xcheckedClass     : 'ux-form-cb-nullchecked',
	xcheckedVal       : '',
	inputValue        : 'true',
	uncheckedVal      : 'false',

	getSubmitValue    : function()
	{
		if(this.xchecked) return this.xcheckedVal;
		return this.callParent(arguments)
	},

	getValue          : function()
	{
		return (this.xchecked)?(null):(this.callParent(arguments));
	},

	initValue         : function()
	{
		if(Ext.get(this.name) && ZeTS.ises(Ext.get(this.name).getValue()))
			this.setRawValue(this.xcheckedVal)
		else
			this.callParent(arguments)
	},

	setRawValue       : function(v)
	{
		if(this.xcheckedVal === v)
		{
			this.xchecked = true
			v = false
		}

		if(!this.xchecked)
		{
			this.removeCls(this.xcheckedClass)
			return this.callParent([v])
		}
		else
		{
			this.callParent([v])
			this.addCls(this.xcheckedClass)
			return false
		}
	},

	onBoxClick        : function(e)
	{
		if(this.disabled || this.readOnly) return

		//?: {checked -> null-checked}
		if(this.checked)
		{
			this.xchecked = true
			this.setValue(false)
		}
		//?: {null-checked -> unchecked}
		else if(this.xchecked)
		{
			this.xchecked = false
			this.setValue(false)
		}
		//?: {unchecked -> checked}
		else
			this.setValue(true)
	}
})


// +----: Disabled Selection Model :-----------------------------+

Ext.define('Ext.ux.selection.No',
{
	extend            : 'Ext.selection.RowModel',

	doSelect          : function()
	{},

	doMultiSelect     : function()
	{}
})


// +----: Grid Row Action Cell :---------------------------------+

/**
 * Action column that activates it's first
 * action when user clicks the cell, not the icon.
 */
Ext.define('Ext.ux.column.ActionCell',
{
	extend            : 'Ext.grid.column.Action',

	processEvent      : function(type, view, cell, recordIndex, cellIndex, e, record, row)
	{
		//?: {clicked not in the cell}
		if((type == 'click') && !e.getTarget().className ||
		   !e.getTarget().className.match(this.actionIdRe)
		  )
		{
			var target = Ext.get(e.getTarget()).down('.' + Ext.baseCSSPrefix + 'action-col-0', true);
			if(target) e.target = target;
		}

		return this.callParent(arguments);
	},

	defaultRenderer   : function(v, meta)
	{
		var res = this.callParent(arguments);
		meta.tdCls += ' retrade-action-cell';
		return res;
	}
})


// +----: Multi Field :------------------------------------------+

/**
 * Container of fields that displays only one ov them.
 * Primary used for grids cell editing when a column
 * has values from different types.
 *
 * As a container the field may have not only a field
 * components (but also labels, blocks). If a field
 * is a direct child, you may use index of the child
 * items. Be aware when adding-removing them.
 */
Ext.define('Ext.ux.field.Multi',
{
	extend            : 'Ext.Container',

	mixins            : [ 'Ext.form.field.Field' ],

	layout            : 'fit',

	/**
	 * Tells index of currently active field,
	 * or directly refers child field component.
	 */
	active            : 0,

	initComponent     : function()
	{
		this.callParent()

		//~: show active field
		this.$show_active()

		//~: show-hide on added
		this.on('added',
		  ZeT.fbind(this.$show_active, this))
	},

	/**
	 * Return currently active field.
	 */
	getActive         : function()
	{
		var field = this.active

		//?: {take by the index}
		if(ZeT.isi(field))
			if(field < this.items.getCount())
				field = this.items.getAt(field)

		//?: {is not a component}
		if(field && (field.isFormField !== true))
			field = undefined

		return field
	},

	setActive         : function(f)
	{
		//?: {clear active}
		if(ZeT.isx(f))
		{
			delete this.active
			this.$show_active()
			return this
		}

		//?: {field is given by index}
		if(ZeT.isi(f))
			if(f < this.items.getCount())
				f = this.items.getAt(f)

		//?: {not a field}
		ZeT.assert(f && (f.isFormField === true))

		//?: {not within the descendants}
		ZeT.assert(ZeT.ii(this.query(
		  '[isFormField=true]'), f))

		//~: assign and show
		this.active = f
		this.$show_active()

		return this
	},

	/**
	 * First, hides all the children. Then, traces
	 * up from the active child and shows it and
	 * all it's ancestor containers.
	 */
	$show_active      : function()
	{
		//~: hide all children
		this.items.each(function(i){ i.hide() })

		//~: take the active, show up
		var a = this.getActive()
		while(a && (a != this))
		{
			a.show()
			a = a.ownerCt
		}
	},

	/**
	 * Applies call to currently active field.
	 */
	$apply            : function(method, args)
	{
		var m, field = this.getActive()

		//?: {no field or not supports}
		if(!field || !ZeT.isf(m = field[method]))
			return undefined

		return m.apply(field, args)
	},

	/**
	 * Applies call to each nested field component.
	 */
	$each            : function(method, args)
	{
		var fs = this.query('[isFormField=true]')

		for(var m, i = 0;(i < fs.length);i++)
			if(ZeT.isf(m = fs[i][method]))
				m.apply(fs[i], args)

		return this
	}
})

/**
 * Wraps methods from field-related components
 * and the mix-ins with $apply and $each.
 */
ZeT.scope(Ext.ux.field.Multi, function(Multi)
{
	var Mx = {}

	var A_FIELD = [ 'batchChanges', 'checkChange',
	  'checkDirty', 'extractFileInput', 'getErrors',
	  'getModelData', 'getName', 'getSubmitData',
	  'getValidation', 'getValue', 'initField',
	  'initValue', 'isDirty', 'isEqual', 'isFileUpload',
	  'isValid', 'markInvalid', 'setValidation',
	  'setValue', 'validate'
	]

	var E_FIELD = [ 'clearInvalid', 'reset',
	  'resetOriginalValue'
	]

	var A_BASE = [ 'getInputId', 'getRawValue',
	  'getSubmitValue', 'processRawValue', 'rawToValue',
	  'setFieldStyle', 'setRawValue', 'setReadOnly',
	  'validateValue', 'valueToRaw'
	]

	var A_OBS = [ 'fireAction', 'fireEvent', 'fireEventArgs',
	  'getConfig', 'getInitialConfig', 'hasListener',
	  'isSuspended', 'relayEvents', 'setConfig'
	]

	var E_OBS = [ 'addListener', 'addManagedListener',
	  'clearListeners', 'clearManagedListeners',
	  'enableBubble', 'mon', 'mun', 'on', 'removeListener',
	  'removeManagedListener', 'setListeners', 'resumeEvent',
	  'resumeEvents', 'suspendEvent', 'suspendEvents', 'un'
	]

	var A_FO = [ 'focus', 'isFocusable',
	  'getTabIndex', 'setTabIndex'
	]

	function apply(m)
	{
		Mx[m] = function()
		{
			return this.$apply(m, arguments)
		}
	}

	function each(m)
	{
		Mx[m] = function()
		{
			return this.$each(m, arguments)
		}
	}

	ZeT.each(A_FIELD, apply)
	ZeT.each(E_FIELD, each)
	ZeT.each(A_BASE,  apply)
	ZeT.each(A_OBS,   apply)
	ZeT.each(E_OBS,   each)
	ZeT.each(A_FO,    apply)

	Multi.addMembers(Mx)
})