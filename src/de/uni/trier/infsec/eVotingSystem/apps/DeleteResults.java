package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


public class DeleteResults {
	public static void main(String[] args) throws IOException{
		
		
		Path dir;
//		for(String path: AppParams.RESULTS){
//			dir = FileSystems.getDefault().getPath(path);
//			try{
//				Files.delete(dir);
//				System.out.println("Deleting file: " + dir);
//			}catch (NoSuchFileException e){
//				System.out.println(e.toString());
//			}
//		}
		
		
		
		dir = FileSystems.getDefault().getPath(AppParams.RESULTS);
		Files.walkFileTree(dir, new FileVisitor<Path>() {
			 @Override
			 public FileVisitResult postVisitDirectory(Path dir, IOException exc)
	                    throws IOException {
	                
	                //System.out.println("Deleting directory: "+ dir);
	                //Files.delete(dir);
	                return FileVisitResult.CONTINUE;
	            }
	 
	            @Override
	            public FileVisitResult preVisitDirectory(Path dir,
	                    BasicFileAttributes attrs) throws IOException {
	                return FileVisitResult.CONTINUE;
	            }
	 
	            @Override
	            public FileVisitResult visitFile(Path file,
	                    BasicFileAttributes attrs) throws IOException {
	                System.out.println("Deleting file: " + file);
	                Files.delete(file);
	                return FileVisitResult.CONTINUE;
	            }
	 
	            @Override
	            public FileVisitResult visitFileFailed(Path file, IOException exc)
	                    throws IOException {
	                System.out.println(exc.toString());
	                return FileVisitResult.CONTINUE;
	            }
	        });
	}
}
