package tntrun;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class VersionChecker {
	
	private static VersionChecker instance;

	public VersionChecker(){
		instance = this;
	}
	
	public static VersionChecker get(){
		return instance;
	}
	
	public String getVersion(){
		try {
			byte[] ver = get(new URL("http://xxxxxxx/updater/tntrun/"));
			String data = new String(ver);
			if(data == null || data.isEmpty()){
				return "error";
			}
			return data;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.WARNING, "[TNTRun] An error was occured while checking version! Please report this here: https://www.spigotmc.org/threads/tntrun.67418/");
			return "error";
		}
	}
	
	public static byte[] get(URL url){
		try{
			HttpURLConnection c = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
	        c.setRequestMethod("GET");
	        c.setRequestProperty("Host", url.getHost());
	        BufferedInputStream in = new BufferedInputStream(c.getInputStream());
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        Streams.pipeStreams(in, out);
	        return out.toByteArray();
	        }catch (IOException e) {
	        	e.printStackTrace();
	        	Bukkit.getLogger().log(Level.WARNING, "[TNTRun] An error was occured while checking version! Please report this here: https://www.spigotmc.org/threads/tntrun.67418/");
	    }
		return null;
	}
}
