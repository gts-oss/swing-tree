package com.globaltcad.swingtree;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a synchronized singleton wrapping a {@link BlockingQueue}.
 */
class EventQueue
{
	private static final EventQueue _INSTANCE = new EventQueue();

	static EventQueue INSTANCE() { return _INSTANCE; }

	/**
	 * This is a simple queue for pending event calls which is thread safe and allows the
	 * GUI thread to register application events.
	 */
	private final BlockingQueue<Runnable> rendererQueue = new LinkedBlockingQueue<>();

	public void register(Runnable task) {
		try {
			rendererQueue.put(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process( int numberOfEventsToBeProcessed, boolean rethrow ) throws InterruptedException {
		int processed = 0;
		while ( processed < numberOfEventsToBeProcessed && !this.rendererQueue.isEmpty() ) {
			try {
				this.rendererQueue.take().run();
			}
			catch (Exception e) {
				if ( rethrow )
					throw e;
				else
					e.printStackTrace();
				processed--;
			}
			processed++;
		}
	}

	public void processAll( boolean rethrow ) throws InterruptedException {
		while ( !this.rendererQueue.isEmpty() )
			process(this.rendererQueue.size(), rethrow);
	}

}
