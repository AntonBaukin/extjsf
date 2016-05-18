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

var cat = readJSON('/Users/anton/Downloads/good-categories.json').categories
var cmap = lo.keyBy(cat, 'id')
var roots = {}

cat.forEach(c =>
{
	let p = c.parentId

	if(!p || !cmap[p])
		return roots[p] = c

	if(!(p = cmap[p])) return

	if(!p.children) p.children = []
	p.children.push(c)
})

lo.keys(roots).forEach(k => delete cmap[k])

var res = []

function process(i)
{
	let c = i.children

	delete i.shortName
	delete i.localizedId
	delete i.children

	if(i.parentId && !cmap[i.parentId])
		delete i.parentId

	res.push(i)

	if(c) c.forEach(process)
}

process(cmap.women)
process(cmap.men)

fs.writeFileSync('/Users/anton/Downloads/good-cats.json', JSON.stringify(res, null, '\t'))
