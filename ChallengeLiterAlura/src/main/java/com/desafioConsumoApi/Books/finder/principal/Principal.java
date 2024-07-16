package com.desafioConsumoApi.Books.finder.principal;

import com.desafioConsumoApi.Books.finder.model.*;
import com.desafioConsumoApi.Books.finder.repository.LibraryRepository;
import com.desafioConsumoApi.Books.finder.service.ConsumAPI;
import com.desafioConsumoApi.Books.finder.service.ConvertData;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Principal {

    private Scanner scanner = new Scanner(System.in);
    private ConsumAPI consumAPI = new ConsumAPI();
    private ConvertData convert = new ConvertData();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private static final String URL_SEARCH = "?search=";
    private final LibraryRepository repository;


    public Principal(LibraryRepository repository) {
        this.repository = repository;
    }

    public void showMenu() {
        var option = -1;
        while (option != 0) {
            var menu = """
                    1 - Buscar libro por titulo 
                    2 - mostrar libros registrados
                    3 - Mostrar Autores registrados
                    4 - Buscar Autores en un determinado año
                    5 - Mostrar libros por idoma
                    6 - Mostrar top 10 de los mejores libros
                    7 - Mostrar estaditicas generales
                    8 - Buscar autor por nombre 
                                        
                                  
                    0 - Salir
                    """;
            while (option != 0) {
                out.println(menu);
                option = scanner.nextInt();
                scanner.nextLine();
                try {

                    switch (option) {
                        case 1:
                            getBookByTitle();
                            break;
                        case 2:
                            showRegisteredBooks();
                            break;
                        case 3:
                            showRegistredAuthors();
                            break;
                        case 4:
                            revealLiveAuthors();

                            break;
                        case 5:
                            findBooksByLanguage();
                            break;
                        case 6:
                            top10Downloads();
                            break;
                        case 7:
                            statistics();
                            break;
                        case 8:
                            searchAuthorByName();
                            break;

                        case 0:
                            break;
                        default:
                            out.println("Opcion invalida!");
                    }
                } catch (NumberFormatException e) {
                    out.println("Opcion invalida!");
                }
            }
        }
    }

    private void getBookByTitle() {
        out.println("Ingrese el nombre del libro que desea buscar: ");
        var bTitle = scanner.nextLine().replace(" ", "%20");
        var json = consumAPI.gettingData(URL_BASE+URL_SEARCH+ bTitle);
        var bdata= convert.gettingData(json, Data.class);

        Optional <BookData> bookSearch = bdata.books().stream()
                .findFirst();
        if (bookSearch.isPresent()) {
            //out.println(bookSearch);
            out.println(
                    "\n----- LIBROS -----\n" +
                    "Titulo: " + bookSearch.get().title() +
                    "\n Autor: " + bookSearch.get().authors().stream().map(a -> a.name()).limit(1).collect(Collectors.joining()) +
                    "\n Lenguaje: " + bookSearch.get().languages().stream().collect(Collectors.joining()) +
                    "\n Numero de Descargas: " + bookSearch.get().downloadCount() +"\n"+
                    "\n-----------------");
            try {
                List<Book> booklook;
                booklook = bookSearch.stream().map(a -> new Book(a)).collect(Collectors.toList());
                Author authorApi = bookSearch.stream()
                        .flatMap(b -> b.authors().stream()
                                .map(a -> new Author(a)))
                        .collect(Collectors.toList()).stream().findFirst().get();

                Optional<Author> authorBd = repository.findAuthorByNameContaining(bookSearch.get().authors().stream()
                        .map(a -> a.name())
                        .collect(Collectors.joining()));

                Optional<Book> optionalBook = repository.getBookContainsEqualsIgnoreCaseTitle(bTitle);


                if (optionalBook.isPresent()) {
                    out.println("Libro ya guardado en la base de datos!");
                } else {
                    Author author;
                    if (authorBd.isPresent()) {
                        author = authorBd.get();
                        out.println("El autor ya existe en la base de datos!");
                    } else {
                        author = authorApi;
                        repository.save(author);
                    }
                    author.setBooks(booklook);
                    repository.save(author);
                }

            } catch (Exception e) {
                System.out.println("Advertencia! " + e.getMessage());
            }
        }else{
            out.println("Libro no encontrado");
        }

        }

        public void searchAuthorByName(){
            if (repository == null) {
                System.out.println("El repositorio no está inicializado!");
                return;
            }
            out.println("Por favor ingrese el nombre del autor: ");
            var name =scanner.nextLine();
            Optional<Author> author =repository.findAuthorByNameContaining(name);


            if (author.isPresent()){
                System.out.println(
                        "\n----- AUTOR -----" +
                        "\nAutor: " + author.get().getName() +
                        "\nFecha de nacimiento: " + author.get().getDateOfBirth() +
                        "\nFecha de fallecimiento: " + author.get().getDateOfDecease() +
                        "\nLibros: " + author.get().getBooks().stream()
                        .map(l -> l.getTitle()).collect(Collectors.toList()) + "\n"+
                        "\n--------------------\n" );
            }else {
                out.println("Este autor no está registrado en la base de datos!");
            }
        }

        public void showRegisteredBooks(){
        List<Book> books = repository.findBooksByAuthor();
        books.forEach(l -> out.println(
                        "------ LIBRO ------" +
                        "\nTitulo: " + l.getTitle() +
                        "\nAutor: " + l.getAuthor().getName() +
                        "\nLenguaje: " + l.getLanguage().getIdiom() +
                        "\nNumero de descargas: " + l.getDownloadCount() +
                        "\n------------------"));
        }
        public void showRegistredAuthors() {
            List<Author> authors = repository.findAll();
            authors.forEach(l -> out.println(
                            "\n----- AUTOR -----" +
                            "\nAutor: " + l.getName() +
                            "\nFecha de nacimiento: " + l.getDateOfBirth() +
                            "\nFecha de fallecimiento: " + l.getDateOfDecease() +
                            "\nLibros: " + l.getBooks().stream()
                            .map(t -> t.getTitle()).collect(Collectors.toList()) + "\n" +
                            "\n------------------------\n"
            ));
        }
        public void revealLiveAuthors(){
            out.println("Escriba el año en el que desea buscar el autor vivo: ");
            try {
                var date =Integer.valueOf(scanner.nextLine());
                List<Author>authors = repository.getAuthorbyDateOfDecease(date);
                if(!authors.isEmpty()){
                    authors.forEach(l -> out.println(
                            "\n----- AUTOR -----" +
                                    "\nAutor: " + l.getName() +
                                    "\nFecha de nacimiento: " + l.getDateOfBirth() +
                                    "\nFecha de Fallecimiento: " + l.getDateOfDecease() +
                                    "\n Libros: " + l.getBooks().stream()
                                    .map(t -> t.getTitle()).collect(Collectors.toList()) + "\n" +
                                    "\n------------------------\n"
                    ));
                }else {
                    out.println("Lo sentimos no se pudo encontrar ningún autor en esta fecha: "+date);
                }
            } catch (NumberFormatException e) {
                out.println("Por favor, introduzca una fecha válida recuerde utilizar solo números ej:1756 "+ e.getMessage());
            }
        }
        public void findBooksByLanguage(){
            var mapLanguages = """
              Seleccione el lenguaje en el que desea buscar el libro
              
              en - English
              es - Spanish
              fr - French
              it - Italian
              pt - Portuguese
              
              """;
            out.println(mapLanguages);
            var lang = scanner.nextLine().toLowerCase();
            if (lang.equalsIgnoreCase("es")
                    || lang.equalsIgnoreCase("en")
                    || lang.equalsIgnoreCase("it")
                    || lang.equalsIgnoreCase("fr")
                    || lang.equalsIgnoreCase("pt")) {
                Language language= Language.fromString(lang);
                List<Book>books = repository.findBookByLanguage(language);
                if (books.isEmpty()){
                    out.println("Lo sentimos, no tenemos ningún libro registrado en este idioma.");
                }else {
                    books.forEach(t-> out.println(
                            "------ Libro ------" +
                            "\nTitulo: " + t.getTitle() +
                            "\nAutor: " + t.getAuthor().getName() +
                            "\nLanguaje: " + t.getLanguage().getIdiom() +
                            "\nNumero de descargas: " + t.getDownloadCount() +
                            "\n------------------"));
                }
            }else{
                out.println("Por favor indique un formato valido para el idioma");
            }
        }

        public void statistics(){
        var json =consumAPI.gettingData(URL_BASE);
        var info = convert.gettingData(json, Data.class);
            IntSummaryStatistics est =info.books().stream()
                    .filter(e -> e.downloadCount() > 0)
                    .collect(Collectors.summarizingInt(BookData::downloadCount));
            Integer average =(int)est.getAverage();
            out.println(
                    "\n---------ESTADISTICAS---------"+
                    "\n Media de cantidad de descargas: " + est.getAverage() +
                    "\n Maximo de cantidad de descargas: " + est.getMax() +
                    "\nMinimo de cantidad de descargas: " + est.getMin() +
                    "\nRegistros evaluados para el calculo: " + est.getCount()

            );
        }

        public void top10Downloads(){
            var json =consumAPI.gettingData(URL_BASE);
            var info = convert.gettingData(json, Data.class);
            info.books().stream()
                .sorted(Comparator.comparing(BookData::downloadCount).reversed())
                .limit(10)
                .map(l -> l.title().toUpperCase())
                .forEach(System.out::println);
        }

    }



