CREATE PROCEDURE dbo.uspAddUser
    @pName NVARCHAR(50),
    @pPassword NVARCHAR(50),
    @responseMessage NVARCHAR(250) OUTPUT
    AS
BEGIN
    SET NOCOUNT ON

    DECLARE @salt UNIQUEIDENTIFIER=NEWID()
    BEGIN TRY

        INSERT INTO USR01_USER (ID, ENABLED, NAME, PASSWORD, SALT)
        VALUES(NEXT VALUE FOR usr01_id_seq, 1, @pName, HASHBYTES('SHA2_512', @pPassword+CAST(@salt AS NVARCHAR(36))), @salt)

    SET @responseMessage='SUCCESS'

    END TRY
    BEGIN CATCH
        SET @responseMessage=ERROR_MESSAGE()
    END CATCH

END
