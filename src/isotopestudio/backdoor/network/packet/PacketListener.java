package isotopestudio.backdoor.network.packet;

public interface PacketListener {

	/**
	 * 
	 * <pre>
	 * This method is called when a packet is received
	 * The method is called once the packet has been read and handled.<pre>
	 * 
	 * @param packet
	 */
	public abstract void received(Packet packet);


	/**
	 * 
	 * <pre>
	 * This method is called when a packet has sent
	 * The method is called once the packet has been read and handled.<pre>
	 * 
	 * @param packet
	 */
	public abstract void sended(Packet packet);
	
}
