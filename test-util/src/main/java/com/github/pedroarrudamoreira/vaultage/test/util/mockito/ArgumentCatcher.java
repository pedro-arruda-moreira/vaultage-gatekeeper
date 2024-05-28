package com.github.pedroarrudamoreira.vaultage.test.util.mockito;

import java.util.function.Consumer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ArgumentCatcher<T> implements Answer<T> {
	@RequiredArgsConstructor
	public class Supplier {
		
		private final Object obj;
		
		public <ST> ST get() {
			return (ST) obj;
		}
		
	}
	
	private int[] positions;
	
	private T returnVal;
	@Getter
	private Object[] capturedValues;
	
	private Consumer<Supplier> consumer;
	
	private int consumerPosition;

	public ArgumentCatcher(int ... positions) {
		this(null, positions);
	}

	public ArgumentCatcher(T returnVal, int ... positions) {
		this.positions = positions;
		this.returnVal = returnVal;
		capturedValues = new Object[positions.length];
	}
	public ArgumentCatcher(T returnVal, Consumer<Supplier> consumer, int consumerPosition) {
		this.consumer = consumer;
		this.returnVal = returnVal;
		this.consumerPosition = consumerPosition;
	}
	public ArgumentCatcher(Consumer<Supplier> consumer, int consumerPosition) {
		this(null, consumer, consumerPosition);
	}

	@Override
	public T answer(InvocationOnMock invocation) throws Throwable {
		if(capturedValues != null) {
			int i = 0;
			for(int currPos : positions) {
				capturedValues[i++] = invocation.getArgument(currPos);
			}
		} else {
			consumer.accept(new Supplier(invocation.getArgument(consumerPosition)));
		}
		return returnVal;
	}

}
