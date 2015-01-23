// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: IPRadixTree.java,v 1.2 2003/04/02 12:59:08 lsw Exp $
package infonet.javasim.FT;

import java.util.ArrayList;
import drcl.inet.data.*;

import infonet.javasim.util.*;

public class IPRadixTree extends infonet.javasim.util.RadixTree
{
	// ----- RadixTree.longest_match ----------------------------------------- //
	/**
	 * This method is a new "rewritten method" of RadixTree youngest_ancestor. The main
	 * problem with this method is that exact mactch isn't supported. To avoid two search
	 * inside the RadixTree, this method will do the search and returns the longest match
	 * (including exact match if the key is found).
	 *
	 * @param bs  The bit string being used to key the search.
	 * @return the data from the longest prefix of the given bit string which has
	 *         non-null data
	 */
	
	public Object longest_match(BitString bs) {
		return lm_helper(root(), bs, 0, null);
	}
	
	// ----- RadixTree.lm_helper -------------------------------------------- //
	/**
	 * A recursive helper for <code>longest_match</code>.
	 *
	 * @param node  The current node being traversed.
	 * @param bs    The bit string being used to key the search.
	 * @param pos   The position in the bit string associated with the 
	 *				current node
	 * @param best	The current longest prefix found.
	 * @return		The data from the longest prefix of the given bit string which has
	 *				non-null data
	 */
	
	private Object lm_helper(RadixTreeNode node, BitString bs, int pos,
							 Object best) {
		
		if(node.data != null) {
			// this is the best match so far so save a pointer to the
			// associated data in case it turns out to be the best overall
			best = node.data;
		}
		
		if(pos == bs.size()) {
			//we're at the end  of thez given string, so return the best match seen
			return best;
		}
		
		if(bs.bgetlr(pos) == Bit.zero) {
			// the next bit is a zero, so follow down the left child
			if (node.left == null) {
				// we're as far as we can go, so return the best match seen
				return best;
			}
			return lm_helper(node.left, bs, pos+1, best);
		} else {
			// the next bit is a one, so follow down the right child
			if (node.right == null) {
				// we're as far as we can go, so return the best match seen
				return best;
			}
			return lm_helper(node.right, bs, pos+1, best);
		}
	}


    	// ----- RadixTree.getAllEntries ------------------------------------------------- //
	/**
     	 * Retreive all information stored inside the IPRadixTree. 
     	 */
    	public ArrayList getAllEntries() {
		ArrayList all = new ArrayList();
        	gae_helper(root(), all);
		return all;
    	}

    	// ----- RadixTree.gae_helper ------------------------------------------ //
    	/**
     	 * A recursive helper for <code>getAllEntries</code>.
     	 *
     	 * @param node  The current node being traversed.
     	 * @param fte_  The current list of data.
     	 */
    	private void gae_helper(RadixTreeNode node, ArrayList fte_) {
        	if (node.data != null) {
            		// we've found a string with an entry, so print it (I put quotes
            		// around each string so that it's easier to tell whether or not
            		// the null string is in the tree)
			fte_.add((FTEntry)node.data);
        	}

        	if (node.left != null) {
            		gae_helper(node.left, fte_);
        	}
        	if (node.right != null) {
            		gae_helper(node.right, fte_);
        	}
    	}


}
