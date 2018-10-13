package org.silcos.roundabouts;

/**
 * <p>
 * Provides an interface for the front-end to interact with <tt>Game</tt>
 * to support animations that occur without user-input. If this feature
 * is not used, then <tt>Game</tt> uses the <tt>nullAdapter</tt> object
 * for compatibility.
 * 
 * <p>
 * An example implementation for this class is given in the package
 * org.silcos.perminan.app at <tt>BoardController</tt> (for JavaFX).
 * 
 * @author Shukant Pal
 */
public class UIAdapter {

	/**
	 * Compatibility object that can be used when the front-end supplies
	 * no <tt>UIAdapter</tt>. It does no operation when any method is
	 * invoked, other than doing finalization stuff.
	 */
	private static UIAdapter nullAdapter = new UIAdapter();
	
	protected UIAdapter() {
	}
	
	/**
	 * Invokes an animation to show the human user that a pebble is being
	 * moved through an external loop (allowing to go on a "long" move and
	 * further capture opponent pebbles). Once the animation is over, the
	 * UI <b>must call</b> <tt>game.notifyLoopInput(sourceRow, sourceColumn)
	 * </tt> <b>to ensure the application works</b>.
	 * 
	 * @param sourceRow
	 * @param sourceColumn
	 */
	public void invokeLoopAnimation(int sourceRow, int sourceColumn) {
		throw new UnsupportedOperationException("Error: visualAdapter failed in"
				+ " invokeLoopAnimation as the front-end didn't register it.");
	}
	
	/**
	 * Returns the value of the static <tt>nullAdapter</tt> property.
	 */
	public static UIAdapter nullAdapter() {
		return (nullAdapter);
	}
	
}
