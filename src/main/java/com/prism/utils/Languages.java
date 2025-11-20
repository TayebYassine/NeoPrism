package com.prism.utils;

import com.prism.services.Service;
import com.prism.services.ServiceForC;
import com.prism.services.ServiceForCPlusPlus;
import com.prism.services.ServiceForJava;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Languages {
    public static String getFullName(File file) {
        String extension = FileUtil.getFileExtension(file);

        return switch (extension.toLowerCase()) {
            case "as" -> "ActionScript";
            case "asm", "s", "inc" -> "Assembly";
            case "c" -> "The C Programming Language";
            case "clj", "cljs", "cljc" -> "Clojure";
            case "cpp" -> "C++";
            case "cs" -> "C#";
            case "css" -> "CSS";
            case "d" -> "D";
            case "dart" -> "Dart";
            case "f90", "for", "f" -> "Fortran";
            case "go" -> "Golang";
            case "groovy" -> "Groovy";
            case "htm", "html", "ejs" -> "HTML";
            case "ini", "properties", "prop", "config" -> "Properties";
            case "java" -> "Java";
            case "js", "mjs", "cjs" -> "JavaScript";
            case "json" -> "JSON";
            case "jsonc" -> "JSON with Comments";
            case "kt" -> "Kotlin";
            case "tex" -> "LaTeX";
            case "less" -> "Less";
            case "lisp", "lsp", "cl" -> "Lisp";
            case "lua" -> "Lua";
            case "md", "markdown" -> "Markdown";
            case "mxml" -> "MXML";
            case "plx", "pls", "pl", "pm", "xs", "t", "pod", "psgi" -> "The Perl Programming Language";
            case "php", "phar", "pht", "phtml", "phs" -> "PHP";
            case "py", "pyw", "pyz", "pyi", "pyc", "pyd" -> "Python";
            case "rb", "ru" -> "Ruby";
            case "rs", "rlib" -> "Rust";
            case "sas" -> "SAS Language";
            case "scala", "sc" -> "Scala";
            case "sql", "sqlite", "db" -> "SQL";
            case "ts", "tsx", "mts", "cts" -> "TypeScript";
            case "sh" -> "Unix Shell";
            case "bat" -> "Windows Batch";
            case "vb" -> "Visual Basic";
            case "xml" -> "XML";
            case "yml", "yaml" -> "YAML";
            case "txt" -> "Text File";
            default -> "Plain Text";
        };
    }

    public static String getHighlighter(File file) {
        String extension = FileUtil.getFileExtension(file);

        return switch (extension) {
            case "as" -> SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
            case "asm", "s", "inc" -> SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86;
            case "c" -> SyntaxConstants.SYNTAX_STYLE_C;
            case "clj", "cljs", "cljc" -> SyntaxConstants.SYNTAX_STYLE_CLOJURE;
            case "cpp" -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case "cs" -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "css" -> SyntaxConstants.SYNTAX_STYLE_CSS;
            case "d" -> SyntaxConstants.SYNTAX_STYLE_D;
            case "dart" -> SyntaxConstants.SYNTAX_STYLE_DART;
            case "f90", "for", "f" -> SyntaxConstants.SYNTAX_STYLE_FORTRAN;
            case "go" -> SyntaxConstants.SYNTAX_STYLE_GO;
            case "groovy" -> SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case "htm", "html", "ejs" -> SyntaxConstants.SYNTAX_STYLE_HTML;
            case "ini", "properties", "prop", "config" -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "js", "mjs", "cjs" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "json" -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case "jsonc" -> SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
            case "kt" -> SyntaxConstants.SYNTAX_STYLE_KOTLIN;
            case "tex" -> SyntaxConstants.SYNTAX_STYLE_LATEX;
            case "less" -> SyntaxConstants.SYNTAX_STYLE_LESS;
            case "lisp", "lsp", "cl" -> SyntaxConstants.SYNTAX_STYLE_LISP;
            case "lua" -> SyntaxConstants.SYNTAX_STYLE_LUA;
            case "md", "markdown" -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case "mxml" -> SyntaxConstants.SYNTAX_STYLE_MXML;
            case "plx", "pls", "pl", "pm", "xs", "t", "pod", "psgi" -> SyntaxConstants.SYNTAX_STYLE_PERL;
            case "php", "phar", "pht", "phtml", "phs" -> SyntaxConstants.SYNTAX_STYLE_PHP;
            case "py", "pyw", "pyz", "pyi", "pyc", "pyd" -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "rb", "ru" -> SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "rs", "rlib" -> SyntaxConstants.SYNTAX_STYLE_RUST;
            case "sas" -> SyntaxConstants.SYNTAX_STYLE_SAS;
            case "scala", "sc" -> SyntaxConstants.SYNTAX_STYLE_SCALA;
            case "sql", "db" -> SyntaxConstants.SYNTAX_STYLE_SQL;
            case "ts", "tsx", "mts", "cts" -> SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "sh" -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "bat" -> SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case "vb" -> SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC;
            case "xml" -> SyntaxConstants.SYNTAX_STYLE_XML;
            case "yml", "yaml", "yarn" -> SyntaxConstants.SYNTAX_STYLE_YAML;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    public static String getIconName(File file) {
        String ext = FileUtil.getFileExtension(file);

        if (ext.isEmpty()) return "file.svg";

        return switch (ext.toLowerCase()) {
            // Adobe / Apple
            case "as" -> "as.svg";
            case "applescript" -> "applescript.svg";

            // Assembly
            case "asm", "s", "inc" -> "asm.svg";

            // C family
            case "c", "h" -> "c.svg";
            case "cpp", "cc", "cxx", "c++", "hpp", "hh", "hxx", "h++" -> "cpp.svg";
            case "cs", "csx" -> "c-sharp.svg";
            case "objc", "mm" -> "objective-c.svg";

            // Clojure
            case "clj", "cljs", "cljc" -> "clojure.svg";

            // Config
            case "ini", "properties", "prop", "config", "conf", "toml", "env", "env.example" -> "config.svg";

            // CSS / styling
            case "css" -> "css.svg";
            case "less" -> "less.svg";
            case "sass", "scss" -> "sass.svg";
            case "styl", "stylus" -> "stylus.svg";

            // D
            case "d", "di" -> "d.svg";

            // Dart
            case "dart" -> "dart.svg";

            // Database
            case "sql", "sqlite", "db" -> "db.svg";

            // Docker
            case "dockerfile" -> "docker.svg";

            // Elixir
            case "ex", "exs" -> "elixir.svg";

            // Elm
            case "elm" -> "elm.svg";

            // Erlang
            case "erl", "hrl" -> "erlang.svg";

            // F#
            case "fs", "fsx", "fsi" -> "f-sharp.svg";

            // Fortran
            case "f", "for", "f90", "f95", "f03", "f08", "f15" -> "f90.svg";

            // Go
            case "go", "go2" -> "go.svg";

            // Gradle
            case "gradle" -> "gradle.svg";

            // GraphQL
            case "graphql", "gql" -> "graphql.svg";

            // Groovy
            case "groovy", "gvy" -> "groovy.svg";

            // Haskell
            case "hs", "lhs" -> "haskell.svg";

            // HTML
            case "html", "htm", "xhtml", "ejs" -> "html.svg";

            // Java
            case "java", "class" -> "java.svg";

            // JavaScript
            case "js", "mjs", "cjs" -> "javascript.svg";

            // JSON
            case "json" -> "json.svg";
            case "jsonc" -> "jsonc.svg";

            // Julia
            case "jl" -> "julia.svg";

            // Kotlin
            case "kt", "kts" -> "kotlin.svg";

            // LaTeX
            case "tex", "ltx", "sty" -> "tex.svg";

            // Lisp
            case "lisp", "lsp", "cl" -> "lisp.svg";

            // Lua
            case "lua" -> "lua.svg";

            // Markdown
            case "md", "markdown" -> "markdown.svg";

            // Mathematica
            case "nb" -> "mathematica.svg";

            // Matlab
            case "matlab", "m" -> "matlab.svg";

            // Maven
            case "pom.xml" -> "maven.svg";

            // MSBuild
            case "proj", "props", "targets" -> "msbuild.svg";

            // Nim
            case "nim", "nims" -> "nim.svg";

            // Nix
            case "nix" -> "nix.svg";

            // OCaml
            case "ml", "mli" -> "ocaml.svg";

            // Pawn
            case "pwn" -> "pawn.svg";

            // Perl
            case "pl", "pm", "pod", "plx", "pls", "t", "xs", "psgi" -> "perl.svg";

            // PHP
            case "php", "phar", "pht", "phtml", "phps" -> "php.svg";

            // PowerShell
            case "ps1", "psd1", "psm1" -> "powershell.svg";

            // Prisma
            case "prisma" -> "prisma.svg";

            // Prolog
            case "pro" -> "prolog.svg";

            // Puppet
            case "pp" -> "puppet.svg";

            // Python
            case "py", "pyw", "pyi", "pyc", "pyd", "pyz" -> "python.svg";

            // R
            case "r", "rmd" -> "r.svg";

            // Razor
            case "cshtml", "razor" -> "razor.svg";

            // Ruby
            case "rb", "ru", "erb" -> "ruby.svg";

            // Rust
            case "rs", "rlib" -> "rust.svg";

            // SAS
            case "sas" -> "sas.svg";

            // Scala
            case "scala", "sc" -> "scala.svg";

            // Shell
            case "sh", "bash", "zsh", "fish", "ksh" -> "shell.svg";
            case "bat", "cmd" -> "batch.svg";

            // Solidity
            case "sol" -> "solidity.svg";

            // Svelte
            case "svelte" -> "svelte.svg";

            // Swift
            case "swift" -> "swift.svg";

            // Terraform
            case "tf", "tfvars" -> "terraform.svg";

            // TypeScript
            case "ts", "tsx", "mts", "cts" -> "typescript.svg";

            // Vala
            case "vala", "vapi" -> "vala.svg";

            // Verilog
            case "v", "vh" -> "verilog.svg";

            // VHDL
            case "vhd", "vhdl" -> "vhdl.svg";

            // Visual Basic
            case "vb", "vbs" -> "visualbasic.svg";

            // Vue
            case "vue" -> "vue.svg";

            // XML
            case "xml", "xsl", "xslt" -> "xml.svg";

            // YAML
            case "yml", "yaml" -> "yaml.svg";

            // Zig
            case "zig" -> "zig.svg";

            // Images
            case "png", "jpg", "jpeg", "gif", "bmp", "ico", "tiff", "webp", "svg" -> "image.svg";

            // Fonts
            case "ttf", "otf", "woff", "woff2", "eot" -> "font.svg";

            // Video
            case "mp4", "webm", "ogg", "avi", "mov", "mkv" -> "video.svg";

            // Audio
            case "mp3", "wav", "flac", "aac", "m4a" -> "audio.svg";

            // Archive
            case "zip", "tar", "gz", "bz2", "xz", "7z" -> "archive.svg";

            // PDF
            case "pdf" -> "pdf.svg";

            // Excel
            case "xls", "xlsx" -> "excel.svg";

            // PowerPoint
            case "ppt", "pptx" -> "powerpoint.svg";

            // Word
            case "doc", "docx" -> "word.svg";

            // Default
            default -> "file.svg";
        };
    }

    public static List<String> getSupported() {
        List<String> extensions = new ArrayList<String>();
        String[] supportedExtensions = {
                "as",
                "asm",
                "s",
                "inc",
                "c",
                "clj",
                "cljs",
                "cljc",
                "cpp",
                "cs",
                "css",
                "d",
                "dart",
                "f90",
                "for",
                "f",
                "go",
                "groovy",
                "htm",
                "html",
                "ejs",
                "ini",
                "properties",
                "prop",
                "config",
                "java",
                "js",
                "mjs",
                "cjs",
                "json",
                "jsonc",
                "kl",
                "kt",
                "lex",
                "less",
                "lisp",
                "lsp",
                "cl",
                "lua",
                "md",
                "markdown",
                "mxml",
                "plx",
                "pls",
                "pl",
                "pm",
                "xs",
                "t",
                "pod",
                "psgi",
                "php",
                "phar",
                "pht",
                "phtml",
                "phs",
                "py",
                "pyw",
                "rb",
                "ru",
                "rs",
                "rlib",
                "sas",
                "scala",
                "sc",
                "ts",
                "tsx",
                "mts",
                "cts",
                "sh",
                "bat",
                "vb",
                "xml",
                "yml",
                "yaml",
                "log",
                "txt",
                "env",
                "gitignore",
                "git",
                "npmignore",
                "yarn",
                "png",
                "jpeg",
                "jpg",
                "db"
        };

        Collections.addAll(extensions, supportedExtensions);

        return extensions;
    }

    public static Service getService(File file) {
        String extension = FileUtil.getFileExtension(file);

        return switch (extension) {
            case "c" -> new ServiceForC();
            case "cpp" -> new ServiceForCPlusPlus();
            case "java" -> new ServiceForJava();
            default -> null;
        };
    }

    public static boolean isSupported(File file) {
        String extension = FileUtil.getFileExtension(file);
        return getSupported().contains(extension);
    }

    public static ImageIcon getIcon(File file) {
        String iconName = getIconName(file);

        ImageIcon icon = ResourceUtil.getIconFromSVG("icons/languages/" + iconName, 18, 18);

        if (icon != null) {
            return icon;
        } else {
            return ResourceUtil.getIconFromSVG("icons/languages/default.svg", 18, 18);
        }
    }
}
