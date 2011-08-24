package code.google.nfs.rpc.protocol;
/**
 * nfs-rpc
 *   Apache License
 *   
 *   http://code.google.com/p/nfs-rpc (c) 2011
 */
import java.util.ArrayList;
import java.util.List;

import code.google.nfs.rpc.Coders;
import code.google.nfs.rpc.RequestWrapper;
import code.google.nfs.rpc.ResponseWrapper;

/**
 * Common RPC Protocol
 * 
 * Request Protocol
 * 	VERSION(1B):   
 *  TYPE(1B):      request/response 
 *  DATATYPE(1B):  serialize/deserialize type
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  ID(4B):        request id
 *  TIMEOUT(4B):   request timeout
 *  TARGETINSTANCELEN(4B):  target service name length
 *  METHODNAMELEN(4B):      method name length
 *  ARGSCOUNT(4B):          method args count
 *  ARG1TYPELEN(4B):        method arg1 type len
 *  ARG2TYPELEN(4B):        method arg2 type len
 *  ...
 *  ARG1LEN(4B):            method arg1 len
 *  ARG2LEN(4B):            method arg2 len
 *  ...
 *  TARGETINSTANCENAME
 *  METHODNAME
 *  ARG1TYPENAME
 *  ARG2TYPENAME
 *  ...
 *  ARG1
 *  ARG2
 *  ...
 *  
 * Response Protocol
 *  VERSION(1B):   
 *  TYPE(1B):      request/response 
 *  DATATYPE(1B):  serialize/deserialize type
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  KEEPED(1B):    
 *  ID(4B):        request id
 *  LENGTH(4B):    body length
 *  BODY
 *  
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class RPCProtocol implements Protocol {
	
	private static final int REQUEST_HEADER_LEN = 1 * 8 + 5 * 4;
	
	private static final int RESPONSE_HEADER_LEN = 1 * 8 + 2 * 4;
	
	private static final byte VERSION = (byte)1;
	
	private static final byte REQUEST = (byte)0;
	
	private static final byte RESPONSE = (byte)1;
	
	public ByteBufferWrapper encode(Object message,ByteBufferWrapper bytebufferWrapper) throws Exception{
		if(!(message instanceof RequestWrapper) && !(message instanceof ResponseWrapper)){
			throw new Exception("only support send RequestWrapper && ResponseWrapper");
		}
		int id = 0;
		byte type = REQUEST;
		if(message instanceof RequestWrapper){
			try{
				int requestArgTypesLen = 0;
				int requestArgsLen = 0;
				List<byte[]> requestArgTypes = new ArrayList<byte[]>();
				List<byte[]> requestArgs = new ArrayList<byte[]>();
				RequestWrapper wrapper = (RequestWrapper) message;
				String[] requestArgTypeStrings = wrapper.getArgTypes();
				for (String requestArgType : requestArgTypeStrings) {
					byte[] argTypeByte = requestArgType.getBytes();
					requestArgTypes.add(argTypeByte);
					requestArgTypesLen += argTypeByte.length;
				}
				Object[] requestObjects = wrapper.getRequestObjects();
				for (Object requestArg : requestObjects) {
					byte[] requestArgByte = Coders.getEncoder(String.valueOf(wrapper.getDataType())).encode(requestArg);
					requestArgs.add(requestArgByte);
					requestArgsLen += requestArgByte.length;
				}
				byte[] targetInstanceNameByte = wrapper.getTargetInstanceName().getBytes();
				byte[] methodNameByte = wrapper.getMethodName().getBytes();
				id = wrapper.getId();
				int timeout = wrapper.getTimeout();
				int capacity = REQUEST_HEADER_LEN + requestArgTypesLen + requestArgsLen;
				ByteBufferWrapper byteBuffer = bytebufferWrapper.get(capacity);
				byteBuffer.writeByte(VERSION);
				byteBuffer.writeByte(type);
				byteBuffer.writeByte((byte)wrapper.getDataType());
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeInt(id);
				byteBuffer.writeInt(timeout);
				byteBuffer.writeInt(targetInstanceNameByte.length);
				byteBuffer.writeInt(methodNameByte.length);
				byteBuffer.writeInt(requestArgs.size());
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.writeInt(requestArgType.length);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.writeInt(requestArg.length);
				}
				byteBuffer.writeBytes(targetInstanceNameByte);
				byteBuffer.writeBytes(methodNameByte);
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.writeBytes(requestArgType);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.writeBytes(requestArg);
				}
				return byteBuffer;
			}
			catch(Exception e){
				e.printStackTrace();
				// TODO: �����쳣
				// LOGGER.error("serialize request object error",e);
				// TODO: ֱ�Ӵ���һ����Ӧ���أ�������Ҫ�ȵ���ʱ
				throw e;
			}
		}
		else{
			ResponseWrapper wrapper = (ResponseWrapper) message;
			byte[] body = null;
			try{
				body = Coders.getEncoder(String.valueOf(wrapper.getDataType())).encode(wrapper.getResponse());
				id = wrapper.getRequestId();
			}
			catch(Exception e){
				// TODO: �����쳣
				e.printStackTrace();
				// LOGGER.error("serialize response object error",e);
				// ��Ȼ������Ӧ�ͻ��ˣ��Ա�ͻ��˿��ٽӵ���Ӧ����Ӧ�Ĵ���
				wrapper.setResponse(new Exception("serialize response object error",e));
				body = Coders.getEncoder(String.valueOf(wrapper.getDataType())).encode(wrapper.getResponse());
			}
			type = RESPONSE;
			int capacity = RESPONSE_HEADER_LEN + body.length;
			ByteBufferWrapper byteBuffer = bytebufferWrapper.get(capacity);
			byteBuffer.writeByte(VERSION);
			byteBuffer.writeByte(type);
			byteBuffer.writeByte((byte)wrapper.getDataType());
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeInt(id);
			byteBuffer.writeInt(body.length);
			byteBuffer.writeBytes(body);
			return byteBuffer;
		}
	}
	
	public Object decode(ByteBufferWrapper wrapper,Object errorObject) throws Exception{
		final int originPos = wrapper.readerIndex();
		if(wrapper.readableBytes() < 2){
			wrapper.setReaderIndex(originPos);
        	return errorObject;
        }
        byte version = wrapper.readByte();
        // �汾1Э��Ľ�����ʽ
        if(version == (byte)1){
        	byte type = wrapper.readByte();
        	if(type == REQUEST){
        		if(wrapper.readableBytes() < REQUEST_HEADER_LEN -2){
        			wrapper.setReaderIndex(originPos);
        			return errorObject;
        		}
        		int dataType = wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		int requestId = wrapper.readInt();
        		int timeout = wrapper.readInt();
        		int targetInstanceLen = wrapper.readInt();
        		int methodNameLen = wrapper.readInt();
        		int argsCount = wrapper.readInt();
        		int argInfosLen = argsCount * 4 * 2;
        		int expectedLen = argInfosLen + targetInstanceLen + methodNameLen;
        		if(wrapper.readableBytes() < expectedLen){
        			wrapper.setReaderIndex(originPos);
        			return errorObject;
        		}
        		expectedLen = 0;
        		int[] argsTypeLen = new int[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					argsTypeLen[i] = wrapper.readInt();
					expectedLen += argsTypeLen[i]; 
				}
        		int[] argsLen = new int[argsCount];
        		for (int i = 0; i < argsCount; i++) {
        			argsLen[i] = wrapper.readInt();
        			expectedLen += argsLen[i];
				}
        		byte[] targetInstanceByte = new byte[targetInstanceLen];
        		wrapper.readBytes(targetInstanceByte);
        		String targetInstanceName = new String(targetInstanceByte);
        		byte[] methodNameByte = new byte[methodNameLen];
        		wrapper.readBytes(methodNameByte);
        		String methodName = new String(methodNameByte);
        		if(wrapper.readableBytes() < expectedLen){
        			wrapper.setReaderIndex(originPos);
        			return errorObject;
        		}
        		String[] argTypes = new String[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					byte[] argTypeByte = new byte[argsTypeLen[i]];
					wrapper.readBytes(argTypeByte);
					argTypes[i] = new String(argTypeByte);
				}
        		Object[] args = new Object[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					byte[] argByte = new byte[argsLen[i]];
					wrapper.readBytes(argByte);
					args[i] = argByte;
				}
        		RequestWrapper requestWrapper = new RequestWrapper(targetInstanceName, methodName, 
        														   argTypes, args, timeout, requestId, dataType);
        		return requestWrapper;
        	}
        	else if(type == RESPONSE){
        		if(wrapper.readableBytes() < RESPONSE_HEADER_LEN -2){
        			wrapper.setReaderIndex(originPos);
        			return errorObject;
        		}
        		int dataType = wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
        		wrapper.readByte();
            	int requestId = wrapper.readInt();
            	int bodyLen = wrapper.readInt();
            	if(wrapper.readableBytes() < bodyLen){
            		wrapper.setReaderIndex(originPos);
            		return errorObject;
            	}
            	byte[] bodyBytes = new byte[bodyLen];
            	wrapper.readBytes(bodyBytes);
            	ResponseWrapper responseWrapper = new ResponseWrapper();
            	responseWrapper.setRequestId(requestId);
            	responseWrapper.setResponse(bodyBytes);
            	responseWrapper.setDataType(dataType);
	        	return responseWrapper;
        	}
        	else{
        		throw new UnsupportedOperationException("protocol type : "+type+" is not supported!");
        	}
        }
        else{
        	throw new UnsupportedOperationException("protocol version :"+version+" is not supported!");
        }
	}

}
