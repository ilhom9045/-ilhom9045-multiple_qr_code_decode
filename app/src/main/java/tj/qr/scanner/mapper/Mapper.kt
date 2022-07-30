package tj.qr.scanner.mapper

interface Mapper<S, T> {

    fun map(value: S): T

}