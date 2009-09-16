/**
 * Part of the CCNx Java Library.
 *
 * Copyright (C) 2008, 2009 Palo Alto Research Center, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation. 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.ccnx.ccn.io;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.xml.stream.XMLStreamException;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.CCNFlowControl;
import org.ccnx.ccn.impl.security.crypto.ContentKeys;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.content.Header;
import org.ccnx.ccn.io.content.Header.HeaderObject;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.KeyLocator;
import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;
import org.ccnx.ccn.protocol.SignedInfo.ContentType;

/**
 * A versioned output stream that adds a header containing file-level metadata
 * to every stream it outputs (see Header for contents). Reading this
 * content with CCNFileInputStream will allow retrieval of both the 
 * content (including automatic retrieval of the latest version, if desired),
 * and the header.
 */
public class CCNFileOutputStream extends CCNVersionedOutputStream {

	public CCNFileOutputStream(ContentName name, CCNHandle handle) throws IOException {
		this(name, (PublisherPublicKeyDigest)null, handle);
	}

	public CCNFileOutputStream(ContentName name,
						   	   PublisherPublicKeyDigest publisher,
						   	   CCNHandle handle) throws IOException {
		this(name, null, publisher, null, null, handle);
	}

	public CCNFileOutputStream(ContentName name, 
							   ContentKeys keys, 
							   CCNHandle handle) throws IOException {
		this(name, null, null, null, keys, handle);
	}

	public CCNFileOutputStream(ContentName name, 
			  			   	   KeyLocator locator, 
			  			   	   PublisherPublicKeyDigest publisher,
			  			   	   ContentKeys keys,
			  			   	   CCNHandle handle) throws IOException {
		this(name, locator, publisher, null, keys, handle);
	}

	public CCNFileOutputStream(ContentName name, 
							   KeyLocator locator,
							   PublisherPublicKeyDigest publisher, 
							   ContentType type, 
							   ContentKeys keys, 
							   CCNHandle handle)
			throws IOException {
		super(name, locator, publisher, type, keys, handle);
	}
	
	protected CCNFileOutputStream(ContentName name, 
								  KeyLocator locator, 
								  PublisherPublicKeyDigest publisher,
								  ContentType type, 
								  ContentKeys keys,
								  CCNFlowControl flowControl) throws IOException {
		super(name, locator, publisher, type, keys, flowControl);
	}

	/**
	 * Writes the header to the network.
	 * @throws IOException
	 */
	protected void writeHeader() throws IOException {
		// What do we put in the header if we have multiple merkle trees?
		try {
			putHeader(_baseName, lengthWritten(), getBlockSize(), _dh.digest(), null);
		} catch (XMLStreamException e) {
			Log.fine("XMLStreamException in writing header: " + e.getMessage());
			// TODO throw nested exception
			throw new IOException("Exception in writing header: " + e);
		}
	}
	
	/**
	 * Subclasses that want to do something other than write a header at the end
	 * should override this, not close(), because CCNOutputStream.close() currently
	 * calls waitForPutDrain, and we don't want to call that till after we've put the header.
	 * 
	 * When we can, we might want to write the header earlier. Here we wait
	 * till we know how many bytes are in the file.
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws NoSuchAlgorithmException 
	 * @throws SignatureException 
	 * @throws InvalidKeyException 
	 * @throws XMLStreamException 
	 */
	@Override
	protected void closeNetworkData() throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InterruptedException  {
		super.closeNetworkData();
		writeHeader();
	}
	
	/**
	 * Actually put the header blocks (versioned, though that isn't necessary) onto the wire.
	 */
	protected void putHeader(
			ContentName name, long contentLength, int blockSize, byte [] contentDigest, 
			byte [] contentTreeAuthenticator) throws XMLStreamException, IOException  {


		ContentName headerName = SegmentationProfile.headerName(name);
		Header headerData = new Header(contentLength, contentDigest, contentTreeAuthenticator, blockSize);
		// DKS TODO -- deal with header encryption, making sure it has same publisher as
		// rest of file via the segmenter
		// The segmenter contains the flow controller. Should do the right thing whether this
		// is a raw stream or a repo stream. It should also already have the keys. Could just share
		// the segmenter. For now, use our own.
		Log.info("Writing header.");
		HeaderObject header = new HeaderObject(headerName, headerData, this._publisher, this._locator, this.getSegmenter().getLibrary());
		header.save();
		Log.info("Wrote header: " + header.getVersionedName());
	}
}
