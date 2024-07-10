package com.LiterAlura.LiterAlura.principal;

import com.LiterAlura.LiterAlura.model.Autor;
import com.LiterAlura.LiterAlura.model.Datos;
import com.LiterAlura.LiterAlura.model.DatosLibros;
import com.LiterAlura.LiterAlura.model.DatosAutor;
import com.LiterAlura.LiterAlura.model.Libro;
import com.LiterAlura.LiterAlura.repository.AutorRepository;
import com.LiterAlura.LiterAlura.repository.LibroRepository;
import com.LiterAlura.LiterAlura.service.ConsumoApi;
import com.LiterAlura.LiterAlura.service.ConvierteDatos;

import java.util.*;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private List<Autor> autores;
    private List<Libro> libros;
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {

        var opcion = -10;
        try {
            while (opcion != 0) {
                var menu = """
                                            
                        1) Buscar Libro por TITULO
                        2) Listado de LIBROS registrados
                        3) Listado de AUTORES registrados
                        4) Listado de AUTORES vivos en un determinado año
                        5) Listado de LIBROS por IDIOMA
                                            
                        0) Salir
                                            
                        Por favor ingrese la opción deseada:
                        """;
                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listaDeLibrosRegistrados();
                        break;
                    case 3:
                        listaDeAutoresRegistrados();
                        break;
                    case 4:
                        listaDeAutoresVivosEnDeterminadoAnio();
                        break;
                    case 5:
                        listaDeLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Cerrando aplicacion. Hasta pronto.");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        break;

                }
            }
        } catch (InputMismatchException e) {
            System.out.println("ERROR! Volver a ejecutar aplicación y seleccione una opción del menú.");
        }
    }

    private Datos buscarDatosLibros() {
        System.out.println("Ingrese el nombre del libro a buscar: ");
        var libro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + libro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);
        return datos;
    }

    private Libro agregarLibroBD(DatosLibros datosLibros, Autor autor) {
        Libro libro = new Libro(datosLibros, autor);
        return libroRepository.save(libro);

    }

    private void buscarLibroPorTitulo() {
        Datos datos = buscarDatosLibros();

        if (!datos.resultados().isEmpty()) {
            DatosLibros datosLibros = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibros.autor().get(0);
            Libro libroBuscado = libroRepository.findByTituloIgnoreCase(datosLibros.titulo());

            if (libroBuscado != null) {
                System.out.println(libroBuscado);
                System.out.println("El libro ya existe en los registros. No se puede volver a ingresar.");

            } else {
                Autor autorBuscado = autorRepository.findByNombreIgnoreCase(datosAutor.nombre());

                if (autorBuscado == null) {
                    Autor autor = new Autor(datosAutor);
                    autorRepository.save(autor);
                    Libro libro = agregarLibroBD(datosLibros, autor);
                    System.out.println(libro);
                } else {
                    Libro libro = agregarLibroBD(datosLibros, autorBuscado);
                    System.out.println(libro);
                }
            }
        } else {
            System.out.println("El libro no existe. Intente otra busqueda.");
        }
    }

    private void listaDeLibrosRegistrados() {
        libros = libroRepository.findAll();
        if (!libros.isEmpty()) {
            libros.stream().forEach(System.out::println);
        } else {
            System.out.println("No hay Libros registrados.");
        }
    }

    private void listaDeAutoresRegistrados() {
        autores = autorRepository.findAll();
        if (!autores.isEmpty()) {
            autores.stream().forEach(System.out::println);
        } else {
            System.out.println("No hay Autores registrados");
        }
    }

    private void listaDeAutoresVivosEnDeterminadoAnio() {
        System.out.println("Ingresar año de autores vivos: ");
        String fecha = teclado.nextLine();
        try {
            List<Autor> autoresVivosEnCiertaFecha = autorRepository.autorVivoEnDeterminadoAnio(fecha);
            if (!autoresVivosEnCiertaFecha.isEmpty()) {
                autoresVivosEnCiertaFecha.stream().forEach(System.out::println);
            } else {
                System.out.println("No existen Autores vivos en la busqueda.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void listaDeLibrosPorIdioma() {
        System.out.println("""
                1) Español (ES)
                2) Inglés (EN)
                3) Francés (FR)
                4) Portugués (PT)
                                
                5) Volver al menú principal
                                
                Ingresar número de opción para elegir idioma de los libros a buscar:
                """);
        int opcion;
        opcion = teclado.nextInt();
        teclado.nextLine();
        switch (opcion) {
            case 1:
                libros = libroRepository.findByIdiomasContaining("es");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros en Español.");
                }
                break;
            case 2:
                libros = libroRepository.findByIdiomasContaining("en");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros en Inglés.");
                }
                break;
            case 3:
                libros = libroRepository.findByIdiomasContaining("fr");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros en Francés.");
                }
                break;
            case 4:
                libros = libroRepository.findByIdiomasContaining("pt");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros en Portugués.");
                }
                break;
            case 5:
                muestraElMenu();
                break;
            default:
                System.out.println("La opción ingresada no es válida.");
        }
    }
}