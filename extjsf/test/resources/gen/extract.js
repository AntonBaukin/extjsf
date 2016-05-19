const assert = require('assert')
const fs = require('fs')
const lo = require('lodash')

function readFile(file)
{
	if(!lo.endsWith(file, '.gz'))
		return fs.readFileSync(file, { encoding: 'UTF-8' })
	else
		return zlib.gunzipSync(fs.readFileSync(file)).toString('UTF-8')
}

function readJSON(file)
{
	var s = readFile(file)
	assert(lo.isString(s))

	var o = JSON.parse(s)
	assert(lo.isObject(o))

	return o
}

var cats = readJSON('/Users/anton/Downloads/good-cats.json')
var cmap = lo.keyBy(cats, 'id')
cats.forEach((c, i) => c.key = '' + i)

var src = readJSON('/Users/anton/Downloads/goods.src.json')
console.log('Processing %d source goods...', src.length)

var colors = {}, sizes = {}, brands = {}, retailers = {}, goods = []

src.forEach(p =>
{
	var  g = { id: '' + p.id, name: p.name, de: p.description,
	  price: { pr: '' + p.price, cu: p.currency },
	  brand: lo.isObject(p.brand) && p.brand.id,
	  ret: lo.isObject(p.retailer) && p.retailer.id,
	  sizes: [], colors: [], cats: []
	}

	if(lo.isArray(p.colors)) p.colors.forEach(c =>
	{
		var co = { nm: c.name }
		g.colors.push(co)

		if(lo.isArray(c.canonicalColors) && c.canonicalColors.length)
		{
			co.id = c.canonicalColors[0].id
			if(co.nm == c.canonicalColors[0].name)
				delete co.nm
		}

		if(lo.isArray(c.canonicalColors)) c.canonicalColors.forEach(cc =>
		{
			if(lo.isString(cc.id) && lo.isString(cc.name))
				colors[cc.id] = cc.name
		})
	})

	if(lo.isArray(p.sizes)) p.sizes.forEach(s =>
	{
		var sz = { nm: s.name }
		g.sizes.push(sz)

		if(lo.isObject(s.canonicalSize))
			if(lo.isString(s.canonicalSize.id) && lo.isString(s.canonicalSize.name))
			{
				sizes[s.canonicalSize.id] = s.canonicalSize.name

				sz.id = s.canonicalSize.id
				if(sz.nm == s.canonicalSize.name)
					delete sz.nm
			}
	})

	if(lo.isArray(p.categories)) p.categories.forEach(c =>
	{
		var cc = cmap[c.id]
		if(cc) g.cats.push(cc.key)
	})

	if(lo.isObject(p.brand))
		if(lo.isString(p.brand.id) && lo.isString(p.brand.name))
			brands[p.brand.id] = p.brand.name

	if(lo.isObject(p.retailer))
		if(lo.isString(p.retailer.id) && lo.isString(p.retailer.name))
			retailers[p.retailer.id] = p.retailer.name

	if(!g.sizes.length) delete g.sizes
	if(!g.colors.length) delete g.colors
	if(!g.cats.length) delete g.cats

	goods.push(g)
})

console.log('\tcolors      : %d', lo.keys(colors).length)
console.log('\tsizes       : %d', lo.keys(sizes).length)
console.log('\tbrands      : %d', lo.keys(brands).length)
console.log('\tretailers   : %d', lo.keys(retailers).length)

var data = { categories: cats, sizes, brands, retailers, goods }

fs.writeFileSync('/Users/anton/Downloads/goods.json', JSON.stringify(data))