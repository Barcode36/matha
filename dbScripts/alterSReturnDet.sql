USE [MathaNew]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

ALTER TABLE [dbo].[SReturnDet] ADD SReturnDetId INT IDENTITY(1,1);
GO

ALTER TABLE [dbo].[SReturnDet] ALTER COLUMN [BkNo] nvarchar(30);
GO

ALTER TABLE [dbo].[SReturnDet]  WITH CHECK ADD  CONSTRAINT [FK_SReturnDet_Book] FOREIGN KEY([BkNo])
REFERENCES [dbo].[Book] ([BookNo])
GO

ALTER TABLE [dbo].[SReturnDet] CHECK CONSTRAINT [FK_SReturnDet_Book]
GO
--
--declare @i int  = 1
--
--update SReturnDet
--set SReturnDetId  = @i , @i = @i + 1
--where SReturnDetId is null;

GO


ALTER TABLE [dbo].[SReturnDet] ALTER COLUMN [SReturnDetId] INT NOT NULL;
GO

ALTER TABLE [dbo].[SReturnDet] ADD CONSTRAINT SReturnDet_PK PRIMARY KEY([SReturnDetId]);
GO
