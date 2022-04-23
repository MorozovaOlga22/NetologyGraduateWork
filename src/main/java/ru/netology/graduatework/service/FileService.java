package ru.netology.graduatework.service;


import ru.netology.graduatework.dto.FileDescriptionInResponse;
import ru.netology.graduatework.entities.FileEntity;
import ru.netology.graduatework.repository.FileRepository;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    //Основные методы
    public synchronized void addFile(String filename, byte[] file) {
        fileRepository.save(new FileEntity(filename, file));
    }

    public synchronized void deleteFile(String filename) {
        if (!fileRepository.existsById(filename)) {
            throw new RuntimeException("File " + filename + " not found");
        }
        fileRepository.deleteById(filename);
    }

    public byte[] getFile(String filename) {
        final FileEntity file = getFileByName(filename);
        return file.getFileContent();
    }

    public synchronized void editFileName(String oldFilename, String newFilename) {
        final FileEntity fileEntity = getFileByName(oldFilename);
        final FileEntity newFileEntity = new FileEntity(newFilename, fileEntity.getFileContent());
        fileRepository.save(newFileEntity);
        fileRepository.delete(fileEntity);
    }

    public List<FileDescriptionInResponse> getFileList(int limit) {
        final List<FileEntity> files = fileRepository.getFiles(limit);
        return files.stream()
                .map(file -> new FileDescriptionInResponse(file.getFileName(), file.getFileContent().length))
                .collect(Collectors.toList());
    }

    //Вспомогательные методы
    private FileEntity getFileByName(String filename) {
        return fileRepository.findById(filename).orElseThrow(() ->
                new RuntimeException("File " + filename + " not found"));
    }
}