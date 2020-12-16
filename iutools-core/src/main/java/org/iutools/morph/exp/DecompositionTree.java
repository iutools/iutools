package org.iutools.morph.exp;

import java.util.ArrayList;
import java.util.List;

import org.iutools.linguisticdata.SurfaceFormInContext;

public class DecompositionTree {
	
	public SurfaceFormInContext root;
	public List<DecompositionTree> branches;
	
	public DecompositionTree(SurfaceFormInContext surfaceFormInContext) {
		this.root = surfaceFormInContext;
		this.branches = new ArrayList<DecompositionTree>();
	}
	
	public void addAllBranches(List<DecompositionTree> trees) {
		for (int i=0; i<trees.size(); i++)
			this.branches.add(trees.get(i));
	}
	
	public void addBranch(DecompositionTree tree) {
		this.branches.add(tree);
	}
	
	public List<String> componentsAllBranches() {
		return __componentsAllBranches(root.surfaceForm);
	}
	
	public List<String> __componentsAllBranches(String base) {
		List<String> list = new ArrayList<String>();
		if (branches != null) {
			if (branches.size()==0) {
				return null;
			} else {
				for (int ibr=0; ibr<branches.size(); ibr++) {
					DecompositionTree branchTree = branches.get(ibr);
					String formOfBranchTree = branchTree.root.surfaceForm;
					List<String> branchTreeComponents = branchTree.__componentsAllBranches(base+" "+formOfBranchTree);
					if (branchTreeComponents != null)
						list.addAll(branchTreeComponents);
				}
			}
		} else {
			list.add(base);
			return list;
		}
		return list;
	}
	
	public List<String> toStr() {
		List<String> list = new ArrayList<String>();
		String str = "{"+root.surfaceForm+":"+root.morphemeId+"}";
		if (branches.size()==0)
			list.add(str);
		for (int i=0; i<branches.size(); i++) {
			List<String> branchList = new ArrayList<String>();
			DecompositionTree branchTree = branches.get(i);
			if (branchTree != null) {
				List<String> branchStrs = branchTree.toStr();
				for (int ib=0; ib<branchStrs.size(); ib++) {
					String strIb = str + " " + branchStrs.get(ib);
					branchList.add(strIb);
				}
				list.addAll(branchList);
			}
		}
		
		return list;
	}

	public List<Decomposition> toDecomposition() {
		List<Decomposition> decompositions = new ArrayList<Decomposition>();
		List<String> list = new ArrayList<String>();
		String str = "{"+root.surfaceForm+":"+root.morphemeId+"}";
		if (branches.size()==0)
			list.add(str);
		for (int i=0; i<branches.size(); i++) {
			List<String> branchList = new ArrayList<String>();
			DecompositionTree branchTree = branches.get(i);
			if (branchTree != null) {
				List<String> branchStrs = branchTree.toStr();
				for (int ib=0; ib<branchStrs.size(); ib++) {
					String strIb = str + " " + branchStrs.get(ib);
					branchList.add(strIb);
				}
				list.addAll(branchList);
			}
		}
		for (int i=0; i<list.size(); i++) {
			decompositions.add(new Decomposition(list.get(i)));
		}
		
		return decompositions;
	}

	
}
