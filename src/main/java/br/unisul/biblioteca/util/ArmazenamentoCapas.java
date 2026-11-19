package br.unisul.biblioteca.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Guarda as imagens de capa enviadas pelo administrador em uma pasta do disco
 * (padrão: ./uploads/capas) e devolve o endereço público (/capas/arquivo.ext).
 * O tipo da imagem é conferido pelo conteúdo do arquivo, e o nome é sempre gerado pelo sistema.
 */
@Component
public class ArmazenamentoCapas {

    public static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    public static final String PREFIXO_URL = "/capas/";

    private static final Logger LOG = LoggerFactory.getLogger(ArmazenamentoCapas.class);

    private final Path pasta;

    public ArmazenamentoCapas(@Value("${biblioteca.upload-dir:uploads}") String diretorio) {
        this.pasta = Path.of(diretorio, "capas").toAbsolutePath().normalize();
        try {
            Files.createDirectories(pasta);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de capas: " + pasta, e);
        }
    }

    public Path pasta() {
        return pasta;
    }

    /** Valida e grava a imagem. Retorna o endereço público, por exemplo /capas/3f2a....png. */
    public String salvar(MultipartFile arquivo) {
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A imagem é muito grande. O limite é de 5 MB.");
        }
        try {
            byte[] bytes = arquivo.getBytes();
            String extensao = extensaoPeloConteudo(bytes);
            if (extensao == null) {
                throw new IllegalArgumentException("Envie uma imagem JPG, PNG, GIF ou WEBP.");
            }
            String nome = UUID.randomUUID() + "." + extensao;
            Files.write(pasta.resolve(nome), bytes);
            return PREFIXO_URL + nome;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar a imagem de capa", e);
        }
    }

    /** Apaga uma capa antiga. Ignora endereços que não sejam de capas guardadas aqui. */
    public void apagar(String url) {
        if (url == null || !url.startsWith(PREFIXO_URL)) {
            return;
        }
        Path alvo = pasta.resolve(url.substring(PREFIXO_URL.length())).normalize();
        if (!alvo.startsWith(pasta)) {
            return;
        }
        try {
            Files.deleteIfExists(alvo);
        } catch (IOException e) {
            LOG.warn("Não foi possível apagar a capa {}", alvo, e);
        }
    }

    private static String extensaoPeloConteudo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') {
            return "gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
