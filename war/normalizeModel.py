def normalize(infile, outfile):
	verts, normals, faces = readFile(infile)
	nVerts = normalizeVertices(verts, 0.75);
	dumpFile(outfile, nVerts, normals, faces)
	print "wrote", outfile
	
def readFile(infile):
	verts = []
	normals = []
	faces = []
	f = file(infile, 'r')
	for line in f.readlines():
		if line[0:2] == 'v ':
			s = line[1:]
			nums = s.split()
			position = {'x': float(nums[0]), 'y': float(nums[1]), 'z': float(nums[2])};
			verts.append(position)
		elif line[0:2] == 'vn':
			normals.append(line)
		elif line[0:2] == 'f ':
			faces.append(line)
	return verts, normals, faces
		
def normalizeVertices(verts, maxDistance):
	sumX = sumY = sumZ = 0
	maxDim = 0
	n = len(verts)
	for v in verts:
		sumX = sumX + v['x']
		sumY = sumY + v['y']
		sumZ = sumZ + v['z']
	meanX = sumX / n
	meanY = sumY / n
	meanZ = sumZ / n
	
	normVerts = []
	for v in verts:
		newV = {'x': v['x'] - meanX, 'y': v['y'] - meanY, 'z': v['z'] - meanZ}
		if abs(v['x']) > maxDim:
			maxDim = abs(v['x'])
		if abs(v['y']) > maxDim:
			maxDim = abs(v['y'])
		if abs(v['z']) > maxDim:
			maxDim = abs(v['z'])
		normVerts.append(newV)
		
	scale = maxDistance / maxDim;
	
	for i in range(n):
		normVerts[i]['x'] = normVerts[i]['x'] * scale
		normVerts[i]['y'] = normVerts[i]['y'] * scale
		normVerts[i]['z'] = normVerts[i]['z'] * scale
	return normVerts
						
def dumpFile(outfile, verts, normals, faces):
	f = file(outfile, 'w')
	for v in verts:
		f.write('v ' + str(v['x']) + ' ' + str(v['y']) + ' ' + str(v['z']) + '\n')
	
	f.write('\n')
	for n in normals:
		f.write(n)
		
	f.write('\n')
	for t in faces:
		f.write(t)
	f.close()
		
normalize('rawModels/sedanPoly.objm', 'models/sedan.objm')
normalize('rawModels/pickupPoly.objm', 'models/pickup.objm')
normalize('rawModels/suvPoly.objm', 'models/suv.objm')
normalize('rawModels/minivanPoly.objm', 'models/minivan.objm')
normalize('rawModels/busPoly.objm', 'models/bus.objm')


