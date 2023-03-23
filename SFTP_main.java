package com.sftp.dev;

public class SFTP_main {

	public static void main(String[] args) {
		final String sourcePath = "/opt/sapbc48/server/logs/error.log";
		final String Sizecheck = "/opt/sapbc48/server/packages/SAPBC_FreshServiceIntegration/logs/FileSizechecker.txt";
		final String logCheck = "/opt/sapbc48/server/packages/SAPBC_FreshServiceIntegration/logs/LogValueChecker.txt";
		final String Logger = "/opt/sapbc48/server/packages/SAPBC_FreshServiceIntegration/logs/SFTP_API.log";
		File logger = new File(Logger);

		try {
			// Establishing Connection
			String user = "*****";
			String pass = "*****";
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			String host = "*************";

			// IDATA Pipe Line
			IDataCursor pipelineCursor = pipeline.getCursor();

			BufferedWriter pw = new BufferedWriter(new FileWriter(logger, true));
			Date dt = new Date();
			SimpleDateFormat fm = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			String cdate = fm.format(dt);

			JSch jsch = new JSch();

			// Connecting Session
			Session session = jsch.getSession(user, host);
			session.setPassword(pass);
			session.setConfig(config);
			session.connect();
			ChannelSftp channelsftp = (ChannelSftp) session.openChannel("sftp");
			pw.write(cdate + "\t Session connected: " + session.isConnected() + "\n");
			pw.flush();

			// Connecting Channel
			pw.write(cdate + "\t Connecting SSH Channel.....\n");
			pw.flush();
			channelsftp.connect();
			pw.write(cdate + "\t Channel connected: " + channelsftp.isConnected() + "\n");
			pw.flush();

			// Getting File Size
			long sourcefilesize = channelsftp.lstat(sourcePath).getSize();
			String size = String.valueOf(sourcefilesize);

			File f = new File(Sizecheck);

			long current = 0L;
			long previous = channelsftp.lstat(sourcePath).getSize();

			// Creating FileSizeChecker
			if (!f.exists()) {
				pw.write(cdate + "\t Creating FileSizeChecker.txt File............ \n");
				pw.flush();

				f.createNewFile();
			}

			FileInputStream fis = new FileInputStream(f);
			int iByteCount = fis.read();

			/// checking source filesize and taret filesize
			if (iByteCount == -1 && channelsftp.lstat(sourcePath).getSize() != 0) {
				FileWriter fw = new FileWriter(f);
				fw.write(size);
				fw.close();
			}
			fis.close();

			// Read targetfile
			BufferedReader read = new BufferedReader(new FileReader(f));
			String line = read.readLine();
			long readline = Long.parseLong(line);
			pw.write(cdate + "\t Source File Size: " + sourcefilesize + " in bytes \n");
			pw.flush();
			pw.write(cdate + "\t Target File Size: " + readline + " in bytes \n");
			pw.flush();

			current = readline;

			File log = new File(logCheck);
			if (!log.exists()) {
				pw.write(cdate + "\t Creating logcheck file \n");
				pw.flush();
				log.createNewFile();
			}

			BufferedWriter bwr = new BufferedWriter(new FileWriter(log, true));
			BufferedReader brr = new BufferedReader(new FileReader(log));
			List<String> listStrings = new ArrayList<String>();
			String finaled = "";
			String line2 = "";
			while ((line2 = brr.readLine()) != null) {
				listStrings.add(line2);
			}
			String arr[] = listStrings.toArray(new String[0]);
			for (String error : arr) {
				finaled = finaled + error + "\n";
			}

			// comparing current and previous value
			if (current != previous || current < previous) {
				List<String> listOfStrings = new ArrayList<String>();
				InputStream stream = channelsftp.get(sourcePath);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream));
				String line1;

				// Read line and pass to string array
				while ((line1 = br.readLine()) != null) {
					listOfStrings.add(line1);
				}

				Date d = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String date = formatter.format(d);

				// CreateTicket c = new CreateTicket();

				String array[] = listOfStrings.toArray(new String[0]);

				// checking current date and picking Error line
				String data = "";
				for (String element : array) {
					if (element.contains(date)) {
						if (element.contains("Exception")) {
							if (finaled.contains(element) || element.contains("Connect to SAP gateway failed")
									|| element.contains("JCO_ERROR_SERVER_STARTUP")
									|| element.contains("JCO_ERROR_NULL_HANDLE")
									|| element.contains("JCO_ERROR_COMMUNICATION") || element.contains("No IDoc found")
									|| element.contains("Error 111 - Connection refused")
									|| element.contains("JCO_ERROR_FIELD_NOT_FOUND")
									|| element.contains("JCO_ERROR_CANCELLED")
									|| element.contains("Cannot display pipeline as XML")
									|| element.contains("JCO_ERROR_SYSTEM_FAILURE")) {
								continue;
							} else {
								pw.write(cdate + "\t Ticket Created : " + element + "\n");
								pw.flush();
								IDataUtil.put(pipelineCursor, "element", element);
								bwr.write(element);
								bwr.newLine();
								bwr.flush();
							}
						}
					}
				}

				// Changing logchecking file size
				FileWriter fw = new FileWriter(f);
				fw.write(size);
				fw.close();

				// Closing Commands
				pw.close();
				read.close();
				brr.close();
				bwr.close();
				pipelineCursor.destroy();
				channelsftp.disconnect();
				channelsftp.exit();
				session.disconnect();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
