USE [MathaDist]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

ALTER TABLE [dbo].[PPayment] ALTER COLUMN TxnId INT NULL;
GO